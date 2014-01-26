package ayai.networking

/** Ayai Imports **/
import ayai.gamestate.{Effect, EffectType}
import ayai.actions._
import ayai.components._
import ayai.networking.chat._
import ayai.persistence.{User, UserQuery}
import ayai.apps.Constants

import com.artemis.{Entity, World}
import com.artemis.managers.{TagManager, GroupManager}

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef, Props}

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.util.Random
import scala.collection.{immutable, mutable}
import scala.collection.mutable._

import java.rmi.server.UID
import ayai.apps.GameLoop

class NetworkMessageProcessor(actorSystem: ActorSystem, world: World, socketMap: mutable.ConcurrentMap[String, String]) extends Actor {
  def processMessage(message: NetworkMessage) {
    message match {
      case AddNewCharacter(id: String, x: Int, y: Int) => {
        println("Adding a character: " +  id)
        val p: Entity = world.createEntity
        p.addComponent(new Position(x, y))
        p.addComponent(new Bounds(32, 32))
        p.addComponent(new Velocity(4, 4))
        p.addComponent(new Movable(false, new MoveDirection(0,0)))
        p.addComponent(new Health(100,100))
        p.addComponent(new Room(Constants.STARTING_ROOM_ID))
        p.addComponent(new Character(id))
        p.addComponent(new Weapon(name = "Iron Axe", value = 10,
  weight = 10, range = 0, damage = 5, damageType = "physical"))
//        p.addComponent(new Room(1))
        p.addToWorld
        // world.getManager(classOf[TagManager]).register(id, p)
        world.getManager(classOf[GroupManager]).add(p, "CHARACTERS")
        world.getManager(classOf[GroupManager]).add(p, "ROOM"+Constants.STARTING_ROOM_ID)
      }

      case RemoveCharacter(id: String) => {
        println("Removing character: " + id)
        var p = None : Option[Entity]
        p = Some(world.getManager(classOf[TagManager]).getEntity(socketMap(id)))
        p match {
          case None =>
            System.out.println(s"Can't find character attached to socket $id.")
          case Some(character) =>
            character.deleteFromWorld
            socketMap.remove(id)
        }
      }

      case MoveMessage(webSocket: WebSocketFrameEvent, start: Boolean, direction: MoveDirection) => {
        println("Direction: " + direction.xDirection.toString + ", " + direction.yDirection.toString)
        val id: String = socketMap(webSocket.webSocketId)
        val e: Entity = world.getManager(classOf[TagManager]).getEntity(id)
        val movement = new MovementAction(direction)
        println(e.toString)
//        movement.process(e)
        e.removeComponent(classOf[Movable])
        e.addComponent(new Movable(start, direction))
        world.changedEntity(e)
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
      case AttackMessage(webSocket : WebSocketFrameEvent) => {
        //create a projectile
        println("Created Bullet")
        val id : String = socketMap(webSocket.webSocketId)
        val bulletId = (new UID()).toString
        val initiator: Entity = world.getManager(classOf[TagManager]).getEntity(id)
        val bullet : Entity = world.createEntity
        bullet.addComponent(new Bullet(initiator, 10))
        bullet.addComponent(new Bounds(8,8))
        val position : Position = initiator.getComponent(classOf[Position])
        bullet.addComponent(new Position(position.x, position.y))
        bullet.addComponent(new Velocity(2,2))
        val initMovement : Movable = initiator.getComponent(classOf[Movable])
        if(initMovement.direction.xDirection == 0 && initMovement.direction.yDirection == 0) {
          bullet.addComponent(new Movable(true, new DownDirection()))
        } else {
          bullet.addComponent(new Movable(true, initiator.getComponent(classOf[Movable]).direction))
        }
        bullet.addToWorld
        world.getManager(classOf[GroupManager]).add(bullet, "BULLET")
        world.getManager(classOf[TagManager]).register(bulletId, bullet)
      }

      case ItemMessage(id : String, itemAction : ItemAction) => {

      }

      case SocketCharacterMap(webSocket: WebSocketFrameEvent, id: String) => {
        println(webSocket)
        socketMap(webSocket.webSocketId) = id
      }

      case PublicChatMessage(message: String, sender: String) => {
        // Will do this later - we don't have accounts working quite yet, so we will wait until that is ready
        var sUser = None: Option[User]
        sUser = UserQuery.getByUsername(sender)
        
        sUser match {
          case Some(user) =>
            val mh = new ChatHolder(new PublicChat(message, user))
            actorSystem.actorOf(Props(new ChatReceiver())) ! mh
          case _ =>
            println("Error from PublicChatMessage")
        }
      }
      case _ => println("Error from NetworkMessageProcessor.")
    } 
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => println("Error: from interpreter.")
  }
}
