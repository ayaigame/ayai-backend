package ayai.systems

import crane.{Entity, World}
import crane.EntityProcessingSystem

import ayai.components._

class RespawningSystem() extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Respawn])) { 
  override def process(e : Entity) {
  	var respawn = e.getComponent(classOf[Respawn]) match {
  		case (Some(r : Respawn)) => r
  	}


 //  	if(respawn.isReady(System.currentTimeMillis))) {


	// }
  }
}