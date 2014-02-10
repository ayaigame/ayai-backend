package ayai.systems

import com.artemis.{EntityProcessingSystem, Entity, World}
import ayai.components._

class HealthSystem() extends EntityProcessingSystem(include=List(classOf[Health], classOf[Character]), exclude=List(classOf[Respawn])) {    

  override def process(e : Entity) {
  	val characterMapper = e.getComponent(classOf[Character]) match {
  		case(Some(c : Character)) => c
  	}
  	val healthMapper = e.getComponent(classOf[Health]) match {
  		case(Some(h : Health)) => h
  	}

  }
}