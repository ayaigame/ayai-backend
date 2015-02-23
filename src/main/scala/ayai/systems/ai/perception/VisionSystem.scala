package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Component, Entity, World, EntityProcessingSystem}

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

import org.slf4j.{Logger, LoggerFactory}

object VisionSystem {
  def apply(actorSystem: ActorSystem) = new VisionSystem(actorSystem)
}

class VisionSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Vision])) {
  private val log = LoggerFactory.getLogger(getClass)

  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def calculateLOS(e: Entity, e2: Entity) {

  }
}