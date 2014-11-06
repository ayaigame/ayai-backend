package ayai.systems

import crane.{Entity, EntityProcessingSystem}
import ayai.components._
import akka.actor.ActorSystem

object CooldownSystem {
  def apply(actorSystem: ActorSystem) = new CooldownSystem(actorSystem)
}

/**
** If Cooldown components are ready to be removed then remove them from the entity
**/
class CooldownSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Cooldown])) {
  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent[Cooldown] match {
      case Some(cooldown: Cooldown) =>
      if (cooldown.isReady) {
        e.removeComponent[Cooldown]
      }
    }
  }
}