package ayai.systems

import crane.{Entity, World}
import crane.EntityProcessingSystem

import ayai.components._

class RespawningSystem() extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Respawn])) { 
  override def processEntity(e : Entity, deltaTime : Int) {
  	var respawn = e.getComponent(classOf[Respawn]) match {
  		case (Some(r : Respawn)) => r
  	}


  	if(respawn.isReady(System.currentTimeMillis))) {
		//reset players health to full health
		val health = e.getComponent(classOf[Health]) match {
			case Some(h : Health) => h
		}
		
		health.refill

		e.removeComponent(classOf[Respawn])

	}
  }
}