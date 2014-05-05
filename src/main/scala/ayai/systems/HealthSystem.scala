package ayai.systems

/** Crane Imports **/
import crane.{EntityProcessingSystem, Entity, World}
import ayai.statuseffects._
/** Ayai Imports **/
import ayai.components._

object HealthSystem {
  def apply() = new HealthSystem()
}

/**
** For all entities that contain health and character, but are not in respawn
** If the currenthealth of the character is at or below zero then classify them as dead and respawn them
** NPCRespawningSystem will take care of removing all entities that are not needed 
**/
class HealthSystem() extends EntityProcessingSystem(include=List(classOf[Health], classOf[Character]), 
                                                    exclude=List(classOf[Respawn])) {
  override def processEntity(e: Entity, deltaTime: Int) {
    val character = (e.getComponent(classOf[Character]): @unchecked) match {
      case(Some(c: Character)) => c
    }
    val health = (e.getComponent(classOf[Health]): @unchecked) match {
      case(Some(h: Health)) => h
    }
    //look at the status effects of the character

    if(health.getCurrentValue() <= 0) {
      //attach respawn to entity
      e.components += new Dead()
      e.components += new Respawn(1500, System.currentTimeMillis())
    }

    if(health.getCurrentValue() == 50) {
      val entity = world.createEntity()
      val potion = new Item(50, "potion", 20, 0, new Consumable())
      potion.effects += new Effect("heal", "Heals for 50 hp", "currentHealth", 50, new OneOff(), new Multiplier(1.0))
      entity.components += ItemUse(e, potion, e)

      world.addEntity(entity)
    }
  }
}
