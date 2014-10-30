package ayai.systems

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.gamestate._
import ayai.apps._

/** Crane Imports **/
import crane.{Entity,EntityProcessingSystem}

/** External Imports **/
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

/** Akka Imports **/
import akka.util.Timeout

object MovementSystem {
  def apply() = new MovementSystem()
}

/**
** Entities must have Position, Velocity, Actionable and Character Components
** But will exclude any that contain Transport, Respawn, or Dead components
** Check if characters action is active, if so then process the action
** Then check current position on tilemap and relocate if colliding 
**/
class MovementSystem extends EntityProcessingSystem(include=List(classOf[Position], classOf[Velocity], classOf[Actionable]),
                                                    exclude=List(classOf[Transport], classOf[Respawn], classOf[Dead])) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  private lazy val log = LoggerFactory.getLogger(getClass)

  //this will only move characters who have received a movement key and the current component is still set to True
  override def processEntity(e: Entity, delta: Int) {
  (e.getComponent(classOf[Actionable]),
    e.getComponent(classOf[Position]),
    e.getComponent(classOf[Velocity]),
    e.getComponent(classOf[Bounds])) match {
      case (Some(actionable: Actionable), Some(position: Position), Some(velocity: Velocity), Some(bounds: Bounds)) => {
        val originalPosition = new Position(position.x, position.y)
        //if moving then process for the given direction
        if (actionable.active) {
          // if action is a direction
          actionable.action match {
            case (move: MoveDirection) =>
              move.process(e)
            case _ =>
              log.warn("f9def84: MoveDirection doesn't match anything")
          }
        }

        //will update position in function
        val tileMap = world.asInstanceOf[RoomWorld].tileMap
        tileMap.isPositionInBounds(position)

        //if on tile Collision go back to original position
        val collision = tileMap.onTileCollision(position, bounds)

        //get room and check if player should change rooms
        //add transport to players (roomchanging system will take over)
        if (collision) {
          position.x = originalPosition.x
          position.y = originalPosition.y
        }
      }
      case _ => log.warn("bb12e5d: getComponent failed to get anything")
    }
  }
}
