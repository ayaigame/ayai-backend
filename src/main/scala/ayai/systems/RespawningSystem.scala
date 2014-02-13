package ayai.systems

import crane.{Entity, World}
import crane.EntityProcessingSystem

import ayai.components._

class RespawningSystem() extends EntityProcessingSystem(include=List(classOf[Room], classOf[Character], classOf[Respawn])) { 
  override def processEntity(e : Entity, deltaTime : Int) {
  	var respawn = e.getComponent(classOf[Respawn]) match {
  		case (Some(r : Respawn)) => r
  	}
  	val health = e.getComponent(classOf[Health]) match {
  		case(Some(h : Health)) => h
  	}
    val room = e.getComponent(classOf[Room]) match {
      case Some(r : Room) => r
    }

    val position = e.getComponent(classOf[Position]) match {
      case Some(p : Position) => p 
    }
    
    if(respawn.isReady(System.currentTimeMillis())) {
			health.refill()
      e.removeComponent(classOf[Respawn])
      e.components += new Transport(position, room)
    }
  }
}