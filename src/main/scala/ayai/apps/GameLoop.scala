package ayai.apps

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{ ExecutionContext, Promise }
import java.rmi.server.UID

import ayai.systems.{MovementSystem,CollisionSystem,RoomChangingSystem}
import ayai.gamestate.{Effect, EffectType, GameStateSerializer, CharacterRadius, MapRequest}
import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.{GroupManager, TagManager}
import com.artemis.utils.ImmutableBag


import ayai.components.Character
import ayai.networking._
import com.artemis.ComponentType
import java.lang.Boolean
import ayai.components.Position
import ayai.components._
import ayai.data._
import ayai.utils.IterableBag

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent


import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.{immutable, mutable}
import scala.collection.mutable._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConversions._

import scala.collection.mutable.HashMap

import scala.io.Source

object GameLoop {
  var roomHash : HashMap[Int, Entity] = HashMap.empty[Int, Entity]

  var running : Boolean = _

  def main(args: Array[String]) {
    running = true
    var socketMap: mutable.ConcurrentMap[String, String] = new java.util.concurrent.ConcurrentHashMap[String, String]
    var world: World = new World()
    val networkSystem = ActorSystem("NetworkSystem")
    world.setManager(new GroupManager())
    world.setManager(new TagManager())

    
    var room : Entity = EntityFactory.loadRoomFromJson(world, Constants.STARTING_ROOM_ID, "map3.json")
    roomHash.put(Constants.STARTING_ROOM_ID, room)

    room = EntityFactory.loadRoomFromJson(world, 1, "map2.json")
    roomHash.put(1, room)
    
    //create a room 
    room.addToWorld

    ItemFactory.bootup(world)
    ClassFactory.bootup(world)

    world.setSystem(new MovementSystem(roomHash))
    world.setSystem(new RoomChangingSystem(roomHash))
    world.setSystem(new CollisionSystem(world))
    world.initialize()
    
    //load all rooms


    implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

    val messageQueue = networkSystem.actorOf(Props(new NetworkMessageQueue()), name = (new UID()).toString)
    val interpreter = networkSystem.actorOf(Props(new NetworkMessageInterpreter(messageQueue)), name = (new UID()).toString)
    val messageProcessor = networkSystem.actorOf(Props(new NetworkMessageProcessor(networkSystem, world, socketMap)), name = (new UID()).toString)

    val serializer = networkSystem.actorOf(Props(new GameStateSerializer(world, Constants.LOAD_RADIUS)) , name = (new UID()).toString)


    val receptionist = new SockoServer(networkSystem, interpreter, messageQueue)
    receptionist.run(Constants.SERVER_PORT)

    //GAME LOOP RUNS AS LONG AS SERVER IS UP
    while(running) {
      world.setDelta(1)
      world.process()

      val future = messageQueue ? new FlushMessages() // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]

      result.messages.foreach { message =>
        messageProcessor ! new ProcessMessage(message)
      }

      //used to send json messages
      case class JCharacter(id: String, x: Int, y: Int, currHealth : Int, maximumHealth : Int, roomId : Int)
      case class JBullet(id: String, x: Int, y: Int)
//      case class JMap(id : String, roomId : Int, array)

      var aCharacters: ArrayBuffer[JCharacter] = ArrayBuffer()

      val tagManager = world.getManager(classOf[TagManager])
      val characterEntities =  world.getManager(classOf[GroupManager]).getEntities("CHARACTERS")

      for (characterEntity <- new IterableBag(characterEntities)) {
        //need better way of figuring if something is bullet, or figuring 
        // out what each entity has
        val characterId = characterEntity.getComponent(classOf[Character]).id
        
        if(characterEntity.getComponent(classOf[MapChange]) != null) {
          val map = characterEntity.getComponent(classOf[MapChange])
          val future2 = serializer ? new MapRequest(roomHash(map.roomId))
          val result2 = Await.result(future2, timeout.duration).asInstanceOf[String]
          val actorSelection1 = networkSystem.actorSelection("user/SockoSender"+characterId)
          println(result2)
          actorSelection1 ! new ConnectionWrite(result2)  
          characterEntity.removeComponent(classOf[MapChange])
          world.changedEntity(characterEntity)
        }

        //This is how we get character specific info, once we actually integrate this in.
        val future1 = serializer ? new CharacterRadius(characterId)
        val result1 = Await.result(future1, timeout.duration).asInstanceOf[String]
        val actorSelection = networkSystem.actorSelection("user/SockoSender"+characterId)
        actorSelection ! new ConnectionWrite(result1)
      }

      Thread.sleep(1000 / Constants.FRAMES_PER_SECOND)
    }
  }
}


