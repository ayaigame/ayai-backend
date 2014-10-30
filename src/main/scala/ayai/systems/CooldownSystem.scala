package ayai.systems

import crane.{Component, Entity, World, EntityProcessingSystem}
import ayai.components._
import ayai.networking._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.rmi.server.UID
import ayai.factories._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

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
			if(cooldown.isReady) {
				e.removeComponent[Cooldown]
			}
		}
	}
}