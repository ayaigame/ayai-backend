package ayai.systems

import crane.EntityProcessingSystem
import crane.Entity 

import ayai.components.Room
import ayai.components.Character
import ayai.components.Movable
import ayai.components.Transport
import ayai.components.Position
import ayai.components.MapChange

import scala.collection.mutable.HashMap

/**
	This class will only be used if an entity has a Room, Character, Movable, and Transport attached to it
**/
class RoomChangingSystem(roomHash : HashMap[Long, Entity]) extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Movable], classOf[Transport], classOf[Position])) {

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

		}
	}
}