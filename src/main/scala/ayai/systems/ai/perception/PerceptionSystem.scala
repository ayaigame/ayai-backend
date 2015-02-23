package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Component, Entity, World, EntityProcessingSystem}

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

object PerceptionSystem {
  def apply(actorSystem: ActorSystem) = new PerceptionSystem(actorSystem)
}

class PerceptionSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[SenseComponent])) {
  var senseSystems: ArrayBuffer[System] = new ArrayBuffer[System]()

  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }
}