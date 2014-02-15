package ayai.systems

import crane.EntityProcessingSystem
import crane.Entity

import ayai.components._

class FrameExpirationSystem() extends EntityProcessingSystem(include=List(classOf[Frame])) {
	def processEntity(e : Entity, deltaTime : Int) {
		e.getComponent(classOf[Frame]) match {
			case (Some(frame : Frame)) =>
			if(frame.isReady()) {
				e.kill()
			} 
			frame.frameCounts += 1
			case _ =>
		}
		
	}
}