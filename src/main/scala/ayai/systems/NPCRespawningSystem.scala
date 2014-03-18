package ayai.systems

import crane.{Entity, Component, EntityProcessingSystem}
import ayai.components._

// this may be a system that is taken over by AI, but just need it for demo
case class NPCRespawningSystem extends EntityProcessingSystem(include=List(classOf[Time], 
	classOf[Dead], classOf[Respawnable])) {
	// if npc is dead then respawn them
	def processEntity(entity: Entity, deltaTime:Int) {
		println("Getting time in npc")
		entity.getComponent(classOf[Time]) match {
			case Some(time: Time) =>
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