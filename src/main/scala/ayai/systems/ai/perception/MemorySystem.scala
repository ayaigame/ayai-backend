package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Component, Entity, World, EntityProcessingSystem}

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

object MemorySystem {
  def apply(actorSystem: ActorSystem) = new MemorySystem(actorSystem)
}

class MemorySystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Memory])) {
  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def forgetEntity(e: Entity, toForget: Entity) {

  }

  def calculateMemoryAttenuation(m: Memory, deltaTime: Int) {

  }
}