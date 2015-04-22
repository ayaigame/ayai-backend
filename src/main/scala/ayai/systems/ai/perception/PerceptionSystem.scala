package ayai.systems

import ayai.components.SenseComponent

import crane.{Entity, EntityProcessingSystem}
import akka.actor.ActorSystem
import org.slf4j.LoggerFactory
import scala.collection.mutable.ArrayBuffer

object PerceptionSystem {
  def apply(actorSystem: ActorSystem) = new PerceptionSystem(actorSystem)
}

class PerceptionSystem[S](actorSystem: ActorSystem, include: List[Class[S]] = List(classOf[SenseComponent])) extends EntityProcessingSystem(include=include) {
  var senseSystems: ArrayBuffer[PerceptionSystem[SenseComponent]] = new ArrayBuffer[PerceptionSystem[SenseComponent]]()
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false

  override def processEntity(e: Entity, deltaTime: Int): Unit = {

  }

  def notify(evt: PerceptionEvent): Unit = {
    for (sys <- senseSystems) { sys.notify(evt) }
    if (spamLog) log.warn(evt.main.name + " " + evt.action + " " + evt.target.name)
  }

}