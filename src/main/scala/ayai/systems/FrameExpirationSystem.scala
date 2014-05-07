package ayai.systems

import crane.EntityProcessingSystem
import crane.Entity

import ayai.components._

object FrameExpirationSystem {
  def apply() = new FrameExpirationSystem()
}
/**
** For an entity with a Frame, check if its ready and then kill it
** Else add one to its frameCount
** (Probably should not be killing them, must do some action)
**/
class FrameExpirationSystem() extends EntityProcessingSystem(include=List(classOf[Frame])) {
  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[Frame]) match {
      case (Some(frame: Frame)) =>
      if(frame.isReady) {
        e.kill()
      } 
      frame.framesActive -= 1
      case _ =>
    }
  }
}
