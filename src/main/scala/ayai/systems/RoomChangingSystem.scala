package ayai.systems

import com.artemis.annotations.Mapper
import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.systems.EntityProcessingSystem


import ayai.components.Room
import ayai.components.Character
import ayai.components.Movable
import ayai.components.Transport

class RoomChangingSystem(roomHash : HashMap[], a : Aspect = Aspect.getAspectForAll(classOf[Room], classOf[Character], classOf[Movable], classOf[Transport]) ) extends EntityProcessingSystem(a) {
	@Mapper
	var roomMapper : ComponentMapper[Room] = _
	@Mapper
	var characterMapper : ComponentMapper[Character] = _
	@Mapper
	var movableMapper : ComponentMapper[Movable] = _
	@Mapper
	var mapEventMapper : ComponentMapper[Transport] = _
	override def process(e : Entity) {
		//get information from transport class
		val transportEvent : Transport = mapEventMapper.get(e)
		val roomComponent : Room = roomMapper.get(e)

		//make sure that room exists
		

		//take user out of their rooms
		e.removeComponent(classOf[Transport])

	}
}