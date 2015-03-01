package ayai.systems.ai.pathfinding

import akka.actor.ActorSystem
import ayai.components.Position
import ayai.components.pathfinding.{AStar, Pathfinder}
import ayai.factories.GraphFactory
import ayai.gamestate.RoomWorld
import crane.{Entity, EntityProcessingSystem}

object PathfindingSystem {
  val COMPONENT_INCLUDES: List[AnyRef] = List(
    // TODO: currently Crane doesn't allow for specifying component types by root class (superclass)
    // It would be nice to change this so we could query for all entities which have components inheriting from "Pathfinder"
    classOf[AStar],
    classOf[Position]
  )

  def apply(actorySystem: ActorSystem): PathfindingSystem = new PathfindingSystem(actorySystem)
}

class PathfindingSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include = PathfindingSystem.COMPONENT_INCLUDES) {

  def processEntity(e: Entity, delta: Int): Unit = {
    val roomWorld = world.asInstanceOf[RoomWorld]
    val map = GraphFactory.generateGraph(roomWorld)

    for {
      pathfinding <- e.getComponent(classOf[AStar]).toList.map(_.asInstanceOf[AStar])
      position <- e.getComponent(classOf[Position]).toList.map(_.asInstanceOf[Position])
    } {
//      println(s"Processing entity = ${e.uuid} for AStar at position $position")

      // TODO: replace with real target
      pathfinding.findPath(map, position, position) match {
        case Some(path) => {
          if (path.isEmpty) {
            // We are at the goal
          } else {
            // Change velocity, position, whatever
          }
        }
        case None => {
          // There is no path from start to end
        }
      }
    }
  }
}
