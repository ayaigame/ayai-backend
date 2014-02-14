package ayai.systems

import crane.EntityProcessingSystem
import crane.Entity

import ayai.components.attacks._


class AttackExpirationSystem extends EntityProcessingSystem(include=List(classOf[Attack])) {
	def processEntity(e : Entity, deltaTime : Int) {
		(e.getComponent(classOf[Attack])) match {
			case(Some(ta : TimedAttack)) => 
			if(ta.isReady(System.currentTimeMillis)) {
				world.removeEntity(e)
			}
			case(Some(fa : FrameAttack)) =>
			fa.framesCount += 1
			if(fa.isReady()) {
				world.removeEntity(e)
			}
			case None => println("no attack of the types frame and timed")
		}
	}
}