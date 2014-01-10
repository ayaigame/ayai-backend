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
import ayai.components._
import ayai.maps.GameMap
import ayai.data._

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent


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
    var socketMap: mutable.ConcurrentMap[String, String] = new java.util.concurrent.ConcurrentHashMap[String, String]
    var world: World = new World()
    world.setManager(new GroupManager())
    world.setManager(new TagManager())
    world.setSystem(new MovementSystem(map))
    world.setSystem(new CollisionSystem(world))
    world.initialize()

    implicit val timeout = Timeout(5 seconds)
    val networkSystem = ActorSystem("NetworkSystem")
    val messageQueue = networkSystem.actorOf(Props(new NetworkMessageQueue()), name = (new UID()).toString)
    val interpreter = networkSystem.actorOf(Props(new NetworkMessageInterpreter(messageQueue)), name = (new UID()).toString)
    val messageProcessor = networkSystem.actorOf(Props(new NetworkMessageProcessor(networkSystem, world, socketMap)), name = (new UID()).toString)

    val serializer = networkSystem.actorOf(Props(new GameStateSerializer(world, 50)) , name = (new UID()).toString)

    val receptionist = new SockoServer(networkSystem, interpreter, messageQueue)
    receptionist.run(8007)

    while(running) {
      world.setDelta(1)
      world.process()

      val future = messageQueue ? new FlushMessages() // enabled by the “ask” import
      val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]

      result.messages.foreach { message =>
        messageProcessor ! new ProcessMessage(message)
      }

      //used to send json messages
      case class JPlayer(id: String, x: Int, y: Int, currHealth : Int, maximumHealth : Int)
      case class JBullet(id: String, x: Int, y: Int)

      var aPlayers: ArrayBuffer[JPlayer] = ArrayBuffer()
      var aBullets: ArrayBuffer[JBullet] = ArrayBuffer()

      val tagManager = world.getManager(classOf[TagManager])
      val playerTags: List[String] = tagManager.getRegisteredTags.toList

      for (playerID <- playerTags) {
          //need better way of figuring if something is bullet, or figuring 
          // out what each entity has
          val tempEntity : Entity = tagManager.getEntity(playerID)
          if(tempEntity.getComponent(classOf[Bullet]) != null) {
            val tempPos : Position = tempEntity.getComponent(classOf[Position])
            aBullets += JBullet(playerID, tempPos.x, tempPos.y)
          } else {

            //This is how we get character specific info, once we actually integrate this in.
            serializer ! new PlayerRadius(playerID)

            val tempPos : Position = tempEntity.getComponent(classOf[Position])
            val tempHealth : Health = tempEntity.getComponent(classOf[Health])
            aPlayers += JPlayer(playerID, tempPos.x, tempPos.y, tempHealth.currentHealth, tempHealth.maximumHealth)
          }
      }


      val json = (
        ("type" -> "fullsync") ~
        ("players" -> aPlayers.toList.map{ p =>
        (("id" -> p.id) ~
         ("x" -> p.x) ~
         ("y" -> p.y) ~
         ("currHealth" -> p.currHealth) ~
         ("maximumHealth" -> p.maximumHealth))}) ~
        ("bullets" -> aBullets.toList.map{ n => 
        (("id" -> n.id) ~  
         ("x" -> n.x) ~
         ("y" -> n.y))}))
    
      // println(compact(render(json)))
      val actorSelection = networkSystem.actorSelection("user/SockoSender*")
      actorSelection ! new ConnectionWrite(compact(render(json)))

      Thread.sleep(1000 / 30)
    }
  }
}


