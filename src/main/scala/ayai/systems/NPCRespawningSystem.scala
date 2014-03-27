package ayai.systems

import crane.{Entity, Component, EntityProcessingSystem}
import ayai.components._

object NPCRespawningSystem {
  def apply() = new NPCRespawningSystem()
}
// this may be a system that is taken over by AI, but just need it for demo
class NPCRespawningSystem() extends EntityProcessingSystem(include=List(classOf[Time], classOf[Dead], classOf[NPC])) {
	// if npc is dead then respawn them
	def processEntity(entity: Entity, deltaTime:Int) {
		println("TIME")
		val isRespawnable = entity.getComponent(classOf[Respawnable]) match {
			case Some(r: Respawnable) => true
			case _ => false
			entity.kill
		}
		
		entity.getComponent(classOf[Time]) match {
			case Some(time: Time) =>
				// println("CurrentTime: " + System.currentTimeMillis + " StartedTime: " + time.startTime + " MSActive: " + time.msActive)
				if(time.isReady(System.currentTimeMillis)) {
					entity.removeComponent(classOf[Dead])
					entity.removeComponent(classOf[Time])
					//if entity has health then refill
					entity.getComponent(classOf[Health]) match {
						case Some(health: Health) => health.refill()
						case _ => 
					}
				} 
		}

	}
}