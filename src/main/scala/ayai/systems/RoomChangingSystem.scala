package ayai.systems

/** Ayai Imports **/
import ayai.components._
import ayai.gamestate._
import ayai.persistence.InventoryTable

/** Crane Imports **/
import crane.{Entity, EntityProcessingSystem}

/** Akka Imports **/
import akka.actor.ActorSystem

/** External Imports **/
import scala.collection.mutable.HashMap
import org.slf4j.{Logger, LoggerFactory}

/**
  This class will only be used if an entity has a Room, Character, Movable, and Transport attached to it
**/

object RoomChangingSystem {
  def apply(networkSystem: ActorSystem) = new RoomChangingSystem(networkSystem)
}

class RoomChangingSystem(networkSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Actionable], classOf[Transport], classOf[Position])) {
  private val log = LoggerFactory.getLogger(getClass)
  val userRoomMap = networkSystem.actorSelection("user/UserRoomMap")
  override def processEntity(e: Entity, delta: Int) {
    //get information from transport class
    (e.getComponent(classOf[Transport]),
      e.getComponent(classOf[Room]),
      e.getComponent(classOf[Position])) match {
      case(Some(transport: Transport), Some(room: Room),
        Some(position: Position)) =>
        userRoomMap ! SwapWorld(e.tag, transport.toRoom)
        InventoryTable.saveInventory(e)
        e.removeComponent(classOf[Transport])
      case _ =>
        log.warn("052ef02: getComponent failed to return anything")
    }
  }
}
