package ayai.systems

import crane.{Component, Entity, EntityProcessingSystem}

class LevelingSystem() extends EntityProcessingSystem(include=List(classOf[Experience], classOf[Character])) {
  def processEntity(entity: Entity, deltaTime: Int) {
    entity.getComponent(classOf[Experience], classOf[Character]) match {
      case (Some(experience: Experience), Some(character: Character)) => 
        experience.levelUp()        
      case _ =>
    }
    
  }
}