package ayai.systems

/** Ayai Imports **/
import ayai.components._

/** Crane Imports **/
import crane.{Entity, EntityProcessingSystem}

/** External Imports **/
import scala.collection.mutable.HashMap
import org.slf4j.{Logger, LoggerFactory}

/**
  This class will only be used if an entity has a Room, Character, Movable, and Transport attached to it
**/

class RoomChangingSystem(roomHash : HashMap[Long, Entity]) extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Actionable], classOf[Transport], classOf[Position])) {
  private val log = LoggerFactory.getLogger(getClass)
  override def processEntity(e : Entity, delta : Int) {
    //get information from transport class
    (e.getComponent(classOf[Transport]),
      e.getComponent(classOf[Room]),
      e.getComponent(classOf[Position])) match {
      case(Some(transportEvent : Transport), Some(roomComponent : Room), Some(position : Position)) =>
        //make sure that room exists
        //take user out of room
        world.groups("ROOM"+roomComponent.id) -= e
        e.removeComponent(classOf[Room])
        e.components += new Room(transportEvent.toRoom.id)
        world.groups("ROOM"+transportEvent.toRoom.id) += e
        position.x = transportEvent.startPosition.x
        position.y = transportEvent.startPosition.y
        //take user out of their rooms
        e.removeComponent(classOf[Transport])
        e.components += new MapChange(transportEvent.toRoom.id)
      case _ =>
        log.warn("052ef02: getComponent failed to return anything")
    }
  }
}
