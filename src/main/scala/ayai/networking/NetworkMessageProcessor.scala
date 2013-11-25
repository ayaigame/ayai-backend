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

/** External Imports **/
import scala.util.Random

class NetworkMessageProcessor(actorSystem: ActorSystem, world: World) extends Actor {
  def processMessage(message: NetworkMessage) {
    message match {
      case AddNewPlayer(id: String) => {
        println("Adding a player: " +  id)
        val p: Entity = world.createEntity
        val x: Int = Random.nextInt(750) + 10
        val y: Int = Random.nextInt(260) + 10
        p.addComponent(new Position(x, y))
        p.addComponent(new Bounds(10, 10))
        p.addComponent(new Velocity(1, 1))
        p.addToWorld
        world.getManager(classOf[TagManager]).register(id, p)
        world.getManager(classOf[GroupManager]).add(p, "PLAYERS")
      }
      case MoveMessage(id: String, start: Boolean, direction: MoveDirection) => {
        println("Direction: " + direction.xDirection.toString + ", " + direction.yDirection.toString)
        val e: Entity = world.getManager(classOf[TagManager]).getEntity(id)
        val movement = new MovementAction(direction)
        println(e.toString)
        movement.process(e)
      }
      case _ => println("Error from NetworkMessageProcessor.")
    } 
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => println("Error: from interpreter.")
  }
}
