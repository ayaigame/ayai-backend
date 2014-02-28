package ayai.systems

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.gamestate.{RoomWorld, TileMap}

/** Crane Imports **/
import crane.{Entity,EntityProcessingSystem}

/** External Imports **/
import scala.collection.mutable.HashMap
import org.slf4j.{Logger, LoggerFactory}
import java.util.Map

object MovementSystem {
  def apply() = new MovementSystem()
}

class MovementSystem extends EntityProcessingSystem(include=List(classOf[Position], classOf[Velocity],classOf[Room], classOf[Character], classOf[Actionable]),
                                                    exclude=List(classOf[Transport], classOf[Respawn])) {
  private val log = LoggerFactory.getLogger(getClass)
  //this will only move characters who have received a movement key and the current component is still set to True
  override def processEntity(e: Entity, delta : Int) {
  (e.getComponent(classOf[Actionable]),
    e.getComponent(classOf[Position]),
    e.getComponent(classOf[Velocity]),
    e.getComponent(classOf[Room]),
    e.getComponent(classOf[Bounds])) match {
    case (Some(actionable: Actionable), Some(position: Position), Some(velocity: Velocity), Some(room: Room), Some(bounds: Bounds)) =>
      val originalPosition = new Position(position.x, position.y)
      //if moving then process for the given direction
      if(actionable.active) {
        // if action is a direction
        actionable.action match {
          case (move: MoveDirection) =>
            move.process(e)
          case _ =>
            log.warn("f9def84: MoveDirection doesn't match anything")
        }
      }

      //now check to see if movement has created gone past the map (if so put it at edge)
      //TODO: Replace with room entity
      //val roomEntity: Entity = roomHash(room.id)
      val roomEntity: Entity = new Entity
      //will update position in function
      val tileMap: TileMap = world.asInstanceOf[RoomWorld].tileMap
      tileMap.isPositionInBounds(position)
      //if on tile Collision go back to original position
      val collision = tileMap.onTileCollision(position, bounds)
      //get room and check if player should change rooms
      //add transport to players (roomchanging system will take over)
      if(collision) {
        position.x = originalPosition.x
        position.y = originalPosition.y
      }
      val transport = tileMap.checkIfTransport(position)
      if(transport != null) {
        e.components += transport
      }
      case _ =>
        log.warn("bb12e5d: getComponent failed to get anything")
    }
  }
}
