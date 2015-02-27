package ayai.systems

import ayai.components._
import ayai.actions._

import akka.actor.ActorSystem

import crane.{Component, Entity, World, EntityProcessingSystem}

import scala.collection.mutable.ArrayBuffer

object SoundSystem {
  def apply(actorSystem: ActorSystem) = new SoundSystem(actorSystem)
}

class SoundSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Hearing])) {
  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def calculateSoundPropagation(soundEntity: Entity) {

  }
}