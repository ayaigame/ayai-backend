package ayai.systems

import crane.EntityProcessingSystem
import crane.Entity

import ayai.components.attacks._


class AttackExpirationSystem extends EntityProcessingSystem(include=List(classOf[Attack])) {
	def processEntity(e : Entity, deltaTime : Int) {
		
		println("WE ARE IN")
		// for(comp <- e.components) comp match {
		// 	case (ta : TimedAttack) => 
		// 	if(ta.isReady(System.currentTimeMillis)) {
		// 		world.removeEntity(e)
		// 	}
		// 	case(fa : FrameAttack) =>
		// 	if(fa.isReady()) {
		// 		world.removeEntity(e)
		// 	}
		// 	fa.framesCount += 1

		// 	case _ => 
		// }  
	}
}