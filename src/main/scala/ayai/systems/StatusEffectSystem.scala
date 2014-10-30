package ayai.systems

import crane.{Component, World, Entity, EntityProcessingSystem}
import akka.actor.{Actor,ActorSystem,  ActorRef, Props, OneForOneStrategy}
import ayai.components._
import ayai.statuseffects
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import ayai.apps._

object StatusEffectSystem {
  def apply() = new StatusEffectSystem()
}

/**
** Take status effects on character and apply them to designated areas
**/
class StatusEffectSystem() extends EntityProcessingSystem(include = List(classOf[Character]), exclude = Nil) {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[Health]) match {
      case Some(health: Health) => {
        if (health.currentModifiers.size >= 1) {
          println("Status Effecting")
          println(health.currentModifiers)
        }
        health.updateCachedValue()
      }
      case _ =>
    }
    e.getComponent(classOf[Mana]) match {
      case Some(mana: Mana) => mana.updateCachedValue()
      case _ =>
    }
    e.getComponent(classOf[Experience]) match {
      case Some(experience: Experience) => experience.updateCachedValue()
      case _ =>
    }
    e.getComponent(classOf[Stats]) match {
      case Some(stats: Stats) => stats.updateCachedValue()
      case _ =>
    }
    e.getComponent(classOf[Velocity]) match {
      case Some(velocity: Velocity) => velocity.updateCachedValue()
      case _ =>
    }
  }
}