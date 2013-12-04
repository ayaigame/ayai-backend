package ayai.networking

/** Ayai Imports **/
import ayai.gamestate._
import ayai.actions._
import ayai.components._

import com.artemis.{Entity, World}
import com.artemis.managers.{TagManager, GroupManager}

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.ActorRef

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.util.Random
import scala.collection.{immutable, mutable}
import scala.collection.mutable._
import org.jboss.netty.channel.Channel

class NetworkMessageProcessor(actorSystem: ActorSystem, world: World, socketMap: mutable.ConcurrentMap[Channel, String]) extends Actor {
  def processMessage(message: NetworkMessage) {
    message match {
      case AddNewPlayer(id: String) => {
        println("Adding a player: " +  id)
        val p: Entity = world.createEntity
        val x: Int = Random.nextInt(750) + 32
        val y: Int = Random.nextInt(260) + 32
        p.addComponent(new Position(x, y))
        p.addComponent(new Bounds(32, 32))
        p.addComponent(new Velocity(2, 2))
        p.addComponent(new Movable(false, new MoveDirection(0,0)))
        p.addComponent(new Health(100,100))
        p.addToWorld
        world.getManager(classOf[TagManager]).register(id, p)
        world.getManager(classOf[GroupManager]).add(p, "PLAYERS")
      }
      case MoveMessage(webSocket: WebSocketFrameEvent, start: Boolean, direction: MoveDirection) => {
        println("Direction: " + direction.xDirection.toString + ", " + direction.yDirection.toString)
        val id: String = socketMap(webSocket.channel)
        val e: Entity = world.getManager(classOf[TagManager]).getEntity(id)
        val movement = new MovementAction(direction)
        println(e.toString)
//        movement.process(e)
        e.removeComponent(classOf[Movable])
        e.addComponent(new Movable(start, direction))

        //if(start) {
        //    //remove old movable
        //    //currently not thread safe
        //    e.removeComponent(classOf[Movable])
        //    e.addComponent(new Movable(start, direction))
        //  } else {
        //    e.removeComponent(classOf[Movable])
        //    e.addComponent(new Movable(start, direction))
        //  }
      } // give id of the item, and what action it should do (equip, use, unequip, remove from inventory)
      case ItemMessage(id : String, itemAction : ItemAction) => {

      }
      case SocketPlayerMap(webSocket: WebSocketFrameEvent, id: String) => {
        println(webSocket)
        socketMap(webSocket.channel) = id
      }
      case _ => println("Error from NetworkMessageProcessor.")
    } 
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => println("Error: from interpreter.")
  }
}
