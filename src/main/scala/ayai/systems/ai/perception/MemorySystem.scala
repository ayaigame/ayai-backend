package ayai.systems

import ayai.components._
import ayai.actions._

import crane.{Component, Entity, World, EntityProcessingSystem}

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

object MemorySystem {
  def apply(actorSystem: ActorSystem) = new MemorySystem(actorSystem)
}

class MemorySystem(actorSystem: ActorSystem) extends PerceptionSystem(include=List(classOf[Memory])) {
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false

  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def forgetEntity(e: Entity, toForget: Entity) {

  }

  def calculateMemoryAttenuation(m: Memory, deltaTime: Int) {

  }
}