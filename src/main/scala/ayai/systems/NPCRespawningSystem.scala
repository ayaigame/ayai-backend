package ayai.systems

import crane.{Entity, Component, EntityProcessingSystem}
import ayai.components._
case class NPCRespawningSystem extends EntityProcessingSystem(include=List()) {
	def processEntity(entity: Entity, deltaTime:Int) {
		
	}
}