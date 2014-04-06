package ayai.systems

import crane.{Component, World, Entity, EntityProcessingSystem}
import akka.actor.{Actor,ActorSystem,  ActorRef, Props, OneForOneStrategy}
import ayai.components._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import ayai.apps._

object StatusEffectSystem {
  def apply(actorSystem: ActorSystem) = new StatusEffectSystem(actorSystem)
}

/**
** Take status effects on character and apply them to designated areas
**/
class StatusEffectSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(
                          include=List(classOf[StatusEffectBag]),
                                                    exclude=List()) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[StatusEffectBag]) match {
      case (Some(statusBag: StatusEffectBag)) => 
        for(status <- statusBag.statusEffects) {
          status.process(e)
        }
      case _ =>
    }    
  }
}