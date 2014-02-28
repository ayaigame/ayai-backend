package ayai.apps

/** Ayai Imports **/
import ayai.systems._
import ayai.networking._
import ayai.components._
import ayai.persistence._
import ayai.gamestate.{Effect, EffectType, GameStateSerializer, CharacterRadius, MapRequest}
import ayai.factories._

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Status, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

/** Crane Imports **/
import crane.{Entity, World}

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.concurrent.{ ExecutionContext, Promise }
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.io.Source

import java.rmi.server.UID
import java.lang.Boolean

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import org.slf4j.{Logger, LoggerFactory}


object GameLoop {
  var roomHash : HashMap[Long, Entity] = HashMap.empty[Long, Entity]
  private val log = LoggerFactory.getLogger(getClass)

  var running : Boolean = _

  def main(args: Array[String]) {
    running = true
    DBCreation.ensureDbExists()

    var socketMap: ConcurrentMap[String, String] = new java.util.concurrent.ConcurrentHashMap[String, String]
    var world: World = new World()

    world.createGroup("ROOMS")
    world.createGroup("CHARACTERS")
    var room : Entity = EntityFactory.loadRoomFromJson(world, Constants.STARTING_ROOM_ID, "map3.json")
    roomHash.put(Constants.STARTING_ROOM_ID, room)
    world.createGroup("ROOM"+Constants.STARTING_ROOM_ID)
    world.addEntity(room)

    room = EntityFactory.loadRoomFromJson(world, 1, "map2.json")
    roomHash.put(1, room)
    world.createGroup("ROOM"+1)
    world.addEntity(room)
    //create a room
    //room.addToWorld

    ItemFactory.bootup(world)
    ClassFactory.bootup(world)

    world.addSystem(new MovementSystem(roomHash))
    world.addSystem(new RoomChangingSystem(roomHash))
    world.addSystem(new HealthSystem())
    world.addSystem(new RespawningSystem())
    world.addSystem(new FrameExpirationSystem())
    //world.initialize()

    //load all rooms


    implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
    import ExecutionContext.Implicits.global

    val networkSystem = ActorSystem("NetworkSystem")
    val messageQueue = networkSystem.actorOf(Props(new NetworkMessageQueue()))
    val interpreter = networkSystem.actorOf(Props(new NetworkMessageInterpreterSupervisor(messageQueue)))
    val messageProcessor = networkSystem.actorOf(Props(new NetworkMessageProcessorSupervisor(world, socketMap)))
    val authorization = networkSystem.actorOf(Props(new AuthorizationProcessor()))

    val serializer = networkSystem.actorOf(Props(new GameStateSerializer(world, Constants.LOAD_RADIUS)) , name = (new UID()).toString)

    world.addSystem(new NetworkingSystem(networkSystem, serializer, roomHash))
    world.addSystem(new CollisionSystem(networkSystem))

    val receptionist = new SockoServer(networkSystem, interpreter, messageQueue, authorization)
    receptionist.run(Constants.SERVER_PORT)

    //GAME LOOP RUNS AS LONG AS SERVER IS UP
    while(running) {
      //get the time
      val start = System.currentTimeMillis

      val future = messageQueue ? new FlushMessages() // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]

      val processedMessages = new ArrayBuffer[Future[Any]]
      result.messages.foreach { message =>
        processedMessages += messageProcessor ? message
      }

      Await.result(Future.sequence(processedMessages), 1 seconds)

      world.process()

      val end = System.currentTimeMillis
      if((end - start) < (1000/Constants.FRAMES_PER_SECOND)) {
        Thread.sleep((1000 / Constants.FRAMES_PER_SECOND) - (end-start))
      }
    }
  }
}


