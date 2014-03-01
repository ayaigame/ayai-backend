package ayai.systems

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.gamestate._
import ayai.apps._
import ayai.networking._
/** Crane Imports **/
import crane.{Entity,EntityProcessingSystem}

/** External Imports **/
import scala.collection.mutable.HashMap
import org.slf4j.{Logger, LoggerFactory}
import java.util.Map
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object MovementSystem {
  def apply(actorSystem: ActorSystem) = new MovementSystem(actorSystem)
}

class MovementSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Position], classOf[Velocity],classOf[Room], classOf[Character], classOf[Actionable]),
                                                    exclude=List(classOf[Transport], classOf[Respawn])) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
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
      val roomId:String = tileMap.checkIfTransport(position)
      if(roomId != "") {
        position.x = 100
        position.y = 100
        val future = actorSystem.actorSelection("user/RoomList") ? GetWorldByName("room"+roomId)
        val result = Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]]
        val world = result match {
          case Some(roomWorld: RoomWorld) => roomWorld 
        }
        val future1 = actorSystem.actorSelection("user/UserRoomMap") ? SwapWorld(e.tag, world)
        Await.result(future1, timeout.duration)
        val tileMap = world.tileMap
        val json = ("type" -> "map") ~
                    ("tilemap" -> tileMap.file) ~
                    (tileMap.tilesets.asJson)
        val actorSelection = actorSystem.actorSelection("user/SockoSender"+e.tag)
        actorSelection ! new ConnectionWrite(compact(render(json)))
      }
      case _ =>
        log.warn("bb12e5d: getComponent failed to get anything")
    }
  }
}
