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
  var senseSystems: ArrayBuffer[PerceptionSystem] = new ArrayBuffer[PerceptionSystem]()
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false

  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def notify(evt: PerceptionEvent) = {
    for (PerceptionSystem sys: senseSystems) {
      sys.notify();
    }
    if (spamLog) log.warn(evt.getMain().name + evt.getAction() + evt.getTarget().name)
  }

}