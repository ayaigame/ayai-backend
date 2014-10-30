package ayai.systems

import crane.{Entity, World, EntityProcessingSystem}
import ayai.components._
import ayai.statuseffects._

object ItemSystem {
  def apply() = new ItemSystem()
}

class ItemSystem() extends EntityProcessingSystem(include=List(classOf[ItemUse])) {
  def processEntity(entity: Entity, deltaTime: Int) {
    // what this will do is check the ItemUse initiatior and check if the Item is in the inventory and if so remove one quantity of it
    entity.getComponent[ItemUse] match {
      case Some(itemuse: ItemUse) =>
        itemuse.initiator.getComponent[Inventory] match {
          case Some(inventory: Inventory) => 
            inventory.removeItem(itemuse.item)
            for(effect <- itemuse.getItemEffects()) {
              println(effect.effectType)
              addEffect(itemuse.initiator, effect)
            }
          case _ => 
        }
        
      case _ =>  
    }
    entity.kill()
  }
  /**
  Will return the component that the effect tries to use 
  **/
  def addEffect(entity: Entity, effect: Effect) {
    effect.effectType match {
      case "currentHealth" | "maxHealth" =>
        entity.getComponent[Health] match {
          case Some(health: Health) => 
            println("effecting health")
            health.addEffect(effect)
          case _ => 
        }
      case "currentMana" | "maxMana" =>
        entity.getComponent[Mana] match {
          case Some(mana: Mana) => mana.addEffect(effect)
          case _ => 
        }
      case "experience" =>
        entity.getComponent[Experience] match {
          case Some(experience: Experience) => experience.addEffect(effect)
          case _ => 
        }
      case "velocity" =>
        entity.getComponent[Velocity] match {
          case Some(velocity: Velocity) => velocity.addEffect(effect)
          case _ =>
        }
      case "strength" | "intelligence" | "defense" =>
        entity.getComponent[Stats] match {
          case Some(stats: Stats) => stats.addEffect(effect)
          case _ => 
        }
      case _ => 

    }
  }
}