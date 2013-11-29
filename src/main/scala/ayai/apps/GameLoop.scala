package ayai.apps

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import java.rmi.server.UID

import ayai.systems._
import ayai.gamestate._
import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.{GroupManager, TagManager}
import com.artemis.utils.ImmutableBag


import ayai.components.Player
import ayai.networking._
import com.artemis.ComponentType
import java.lang.Boolean
import ayai.components.Position
import ayai.maps.GameMap
import ayai.data._

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent


import org.jboss.netty.channel.Channel
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.{immutable, mutable}
import scala.collection.mutable._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConversions._


object GameLoop {
  def arrayToString(a: Array[Array[Int]]) : String = {
       val str = for (l <- a) yield l.mkString("[", ",", "]")
       str.mkString("[",",\n","]")
  }
  var running : Boolean = _
  var map : GameMap = new GameMap(10,10)
  def main(args: Array[String]) {
    println("compiled")
    running = true
    var socketMap: mutable.ConcurrentMap[Channel, String] = new java.util.concurrent.ConcurrentHashMap[Channel, String]
    var world: World = new World()
    world.setManager(new GroupManager())
    world.setManager(new TagManager())
    world.setSystem(new MovementSystem(map))
    world.setSystem(new CollisionSystem(world))
    world.initialize()

    println(arrayToString(map.map))
    var firstRoom: Room = GameState.createRoom(arrayToString(map.map))
    val startingRoom = firstRoom.getRoomId
    var firstPlayer = EntityFactory.createPlayer(world, startingRoom, 2, 2)
    world.addEntity(firstPlayer)
    var testItem = EntityFactory.createItem(world,1,3,"ItemTest")
    world.addEntity(testItem)

    implicit val timeout = Timeout(5 seconds)
    val networkSystem = ActorSystem("NetworkSystem")
    val messageQueue = networkSystem.actorOf(Props(new NetworkMessageQueue()), name = (new UID()).toString)
    val interpreter = networkSystem.actorOf(Props(new NetworkMessageInterpreter(messageQueue)), name = (new UID()).toString)
    val messageProcessor = networkSystem.actorOf(Props(new NetworkMessageProcessor(networkSystem, world, socketMap)), name = (new UID()).toString)

    val receptionist = new SockoServer(networkSystem, interpreter)
    receptionist.run(8007)

    while(running) {
      world.setDelta(1)
      world.process()

      val future = messageQueue ? new FlushMessages() // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]

      result.messages.foreach { message =>
        messageProcessor ! new ProcessMessage(message)
      }

      case class JPlayer(id: String, x: Int, y: Int)
 
      var aPlayers: ArrayBuffer[JPlayer] = ArrayBuffer()

      val tagManager = world.getManager(classOf[TagManager])
      val playerTags: List[String] = tagManager.getRegisteredTags.toList

      for (playerID <- playerTags) {
          val tempEntity : Entity = tagManager.getEntity(playerID)
          val tempPos : Position = tempEntity.getComponent(classOf[Position])
          aPlayers += JPlayer(playerID, tempPos.x, tempPos.y)
      }
      
      val json = (
        ("type" -> "fullsync") ~
        ("players" -> aPlayers.toList.map{ p =>
        (("id" -> p.id) ~
         ("x" -> p.x) ~
         ("y" -> p.y))}))
    
      //println(compact(render(json)))
      val actorSelection = networkSystem.actorSelection("user/SockoSender*")
      actorSelection ! new ConnectionWrite(compact(render(json)))

      Thread.sleep(1000 / 30)
    }
  }
}


