package ayai.systems

import crane.EntityProcessingSystem
import crane.Entity

import ayai.components._

object TimeExpirationSystem {
  def apply() = new TimeExpirationSystem()
}

class TimeExpirationSystem() extends EntityProcessingSystem(include=List(classOf[Time])) {
  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[Time]) match {
      case Some(time: Time) => 
      if(time.isReady(System.currentTimeMillis)) {
        e.kill()
      }
      case _ =>
    }


  }
}
