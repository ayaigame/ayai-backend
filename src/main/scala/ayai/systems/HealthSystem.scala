package ayai.systems

/** Crane Imports **/
import crane.{EntityProcessingSystem, Entity, World}

/** Ayai Imports **/
import ayai.components._

object HealthSystem {
  def apply() = new HealthSystem()
}

class HealthSystem() extends EntityProcessingSystem(include=List(classOf[Health], classOf[Character]), exclude=List(classOf[Respawn])) {

  override def processEntity(e : Entity, deltaTime : Int) {
    val character = (e.getComponent(classOf[Character]): @unchecked) match {
      case(Some(c : Character)) => c
    }
    val health = (e.getComponent(classOf[Health]): @unchecked) match {
      case(Some(h : Health)) => h
    }
    //look at the status effects of the character

    if(health.currentHealth <= 0) {
      //attach respawn to entity
      e.components += new Respawn(1500, System.currentTimeMillis())
    }
  }
}
