package ayai.apps

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import java.rmi.server.UID

import ayai.systems._
import ayai.gamestate.{Effect, EffectType, GameStateSerializer, CharacterRadius}
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
  var defaultRoomId : Int = 0

  def arrayToString(a: Array[Array[Int]]) : String = {
       val str = for (l <- a) yield l.mkString("[", ",", "]")
       str.mkString("[",",\n","]")
  }
  var running : Boolean = _
  def main(args: Array[String]) {
    println("compiled")
    running = true
    var socketMap: mutable.ConcurrentMap[String, String] = new java.util.concurrent.ConcurrentHashMap[String, String]
    var world: World = new World()
    world.setManager(new GroupManager())
    world.setManager(new TagManager())
    world.setSystem(new MovementSystem(roomHash))
    world.setSystem(new CollisionSystem(world))
    world.initialize()
    
    val room : Entity = EntityFactory.loadRoomFromJson(world, new UID().hashCode, "map3.json")
    defaultRoomId = new UID().hashCode
    roomHash.put(defaultRoomId, room)
    //create a room 
    room.addToWorld


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
      case class JCharacter(id: String, x: Int, y: Int, currHealth : Int, maximumHealth : Int, roomId : Int)
      case class JBullet(id: String, x: Int, y: Int)
//      case class JMap(id : String, roomId : Int, array)

      var aCharacters: ArrayBuffer[JCharacter] = ArrayBuffer()
      var aBullets: ArrayBuffer[JBullet] = ArrayBuffer()

      val tagManager = world.getManager(classOf[TagManager])
      val characterTags: List[String] = tagManager.getRegisteredTags.toList

      for (characterID <- characterTags) {
          //need better way of figuring if something is bullet, or figuring 
          // out what each entity has
          val tempEntity : Entity = tagManager.getEntity(characterID)
          if(tempEntity.getComponent(classOf[Bullet]) != null) {
            val tempPos : Position = tempEntity.getComponent(classOf[Position])
            aBullets += JBullet(characterID, tempPos.x, tempPos.y)
          } else {

            //This is how we get character specific info, once we actually integrate this in.
            val future = serializer ! new CharacterRadius(characterID)
            //val actorSelection = networkSystem.actorSelection("user/SockoSender/"+characterID)
        
            val tempPos : Position = tempEntity.getComponent(classOf[Position])
            val tempHealth : Health = tempEntity.getComponent(classOf[Health])
            val tempRoom : Room = tempEntity.getComponent(classOf[Room])
            aCharacters += JCharacter(characterID, tempPos.x, tempPos.y, tempHealth.currentHealth, tempHealth.maximumHealth, tempRoom.id)
          }
      }


      val json = (
        ("type" -> "fullsync") ~
        ("maps" -> "/assets/maps/map3.json") ~
        ("characters" -> aCharacters.toList.map{ p =>
        (("id" -> p.id) ~
         ("x" -> p.x) ~
         ("y" -> p.y) ~
         ("currHealth" -> p.currHealth) ~
         ("maximumHealth" -> p.maximumHealth) ~
         ("room" -> p.roomId))}) ~
        ("bullets" -> aBullets.toList.map{ n => 
        (("id" -> n.id) ~  
         ("x" -> n.x) ~
         ("y" -> n.y))}))

        
        //println(compact(render(json)))
        val actorSelection = networkSystem.actorSelection("user/SockoSender*")
        actorSelection ! new ConnectionWrite(compact(render(json)))

      Thread.sleep(1000 / 30)
    }
  }
}


