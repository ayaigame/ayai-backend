package ayai.systems

import com.artemis.annotations.Mapper
import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.EntityProcessingSystem
import com.artemis.managers.GroupManager
import com.artemis.Entity 

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
class RoomChangingSystem(roomHash : HashMap[Int, Entity], a : Aspect = Aspect.getAspectForAll(classOf[Room], classOf[Character], classOf[Movable], classOf[Transport], classOf[Position]) ) extends EntityProcessingSystem(a) {
	@Mapper
	var roomMapper : ComponentMapper[Room] = _
	@Mapper
	var characterMapper : ComponentMapper[Character] = _
	@Mapper
	var movableMapper : ComponentMapper[Movable] = _
	@Mapper
	var mapEventMapper : ComponentMapper[Transport] = _
	@Mapper
	var positionMapper : ComponentMapper[Position] = _ 

	override def process(e : Entity) {
		//get information from transport class
		val transportEvent : Transport = mapEventMapper.get(e)
		val roomComponent : Room = roomMapper.get(e)

		//make sure that room exists
		//take user out of room
		world.getManager(classOf[GroupManager]).remove(e, "ROOM"+roomComponent.id)
		e.removeComponent(roomComponent)
		e.addComponent(new Room(transportEvent.toRoom.id))
		world.getManager(classOf[GroupManager]).add(e, "ROOM"+transportEvent.toRoom.id)
		val position : Position = positionMapper.get(e)
		position.x = transportEvent.startPosition.x
		position.y = transportEvent.startPosition.y
		//take user out of their rooms
		e.removeComponent(classOf[Transport])

		e.addComponent(new MapChange(transportEvent.toRoom.id))
		world.changed(e)
		//send to network
	}
}