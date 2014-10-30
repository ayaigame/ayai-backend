package ayai.systems

import crane.{Entity, Component, EntityProcessingSystem}
import ayai.components._

object NPCRespawningSystem {
  def apply() = new NPCRespawningSystem()
}
/**
** Will respawn any npcs that need to be respawned and will kill any item that has been classfied as "Dead"
** And needs to be removed from the world
**/
class NPCRespawningSystem() extends EntityProcessingSystem(include=List(classOf[Time], classOf[Dead], classOf[NPC])) {
	// if npc is dead then respawn them
	def processEntity(entity: Entity, deltaTime:Int) {
		val isRespawnable = entity.getComponent[Respawnable] match {
			case Some(r: Respawnable) => true
			case _ => 
			entity.kill
			false
		}
		
		entity.getComponent[Time] match {
			case Some(time: Time) =>
				// println("CurrentTime: " + System.currentTimeMillis + " StartedTime: " + time.startTime + " MSActive: " + time.msActive)
				if(time.isReady(System.currentTimeMillis)) {
					entity.removeComponent[Dead]
					entity.removeComponent[Time]
					//if entity has health then refill
					entity.getComponent[Health] match {
						case Some(health: Health) => health.refill()
						case _ => 
					}
				} 
		}

	}
}