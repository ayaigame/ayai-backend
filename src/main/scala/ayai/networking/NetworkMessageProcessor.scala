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

class NetworkMessageProcessor(actorSystem: ActorSystem, world: World) extends Actor {
  def processMessage(message: NetworkMessage) {
    message match {
      case AddNewPlayer(id: String) => {
        println("Adding a player: " +  id)
        val p: Entity = world.createEntity
        p.addComponent(new Position(10, 10))
        p.addComponent(new Bounds(10, 10))
        p.addComponent(new Velocity(1, 1))
        p.addComponent(new Movable(false, new MoveDirection(0,0)))
        p.addToWorld
        world.getManager(classOf[TagManager]).register(id, p)
        world.getManager(classOf[GroupManager]).add(p, "PLAYERS")
      }
      case MoveMessage(id: String, start: Boolean, direction: MoveDirection) => {
        println("Direction: " + direction.xDirection.toString + ", " + direction.yDirection.toString)
        val e: Entity = world.getManager(classOf[TagManager]).getEntity(id)
        val movement = new MovementAction(direction)
        println(e.toString)
//        movement.process(e)
        if(start) {
            //remove old movable
            //currently not thread safe
            e.removeComponent(classOf[Movable])
            e.addComponent(new Movable(start, direction))
          } else {
            e.removeComponent(classOf[Movable])
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