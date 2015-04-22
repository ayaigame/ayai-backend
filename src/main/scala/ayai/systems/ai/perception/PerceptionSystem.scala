package ayai.systems

import ayai.components._

import crane.{Entity, EntityProcessingSystem}
import akka.actor.ActorSystem
import org.slf4j.LoggerFactory
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
    senseSystems.foreach(sys => sys.notify())
    if (spamLog) log.warn(evt.main.name + " " + evt.action + " " + evt.target.name)
  }

}