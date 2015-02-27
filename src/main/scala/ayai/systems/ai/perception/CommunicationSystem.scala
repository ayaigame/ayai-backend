package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Component, Entity, World, EntityProcessingSystem}

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

object CommunicationSystem {
  def apply(actorSystem: ActorSystem) = new CommunicationSystem(actorSystem)
}

class CommunicationSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[SenseComponent])) {
  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def inContact(e: Entity, e2: Entity): Boolean = {
    false
  }

  def communicate(e: Entity, e2: Entity) {

  }
}