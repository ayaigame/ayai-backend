package ayai.systems

import crane.{Component, World, Entity}

object StatusEffectSystem {
  def apply(actorSystem: ActorSystem) = new StatusEffectSystem(actorSystem)
}

class StatusEffectSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(
                          include=List(classOf[StatusEffectBag]),
                                                    exclude=List()) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[StatusEffectBag]) match {
      case (Some(statusBag: StatusEffectBag)) => 
        for(status -> statusBag.statuses) {
          status.process(e)
        }
      case _ =>
    }    
  }
}