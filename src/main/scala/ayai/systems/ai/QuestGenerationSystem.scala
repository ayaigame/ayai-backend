package ayai.systems

import crane.{Entity, EntityProcessingSystem}

import java.rmi.server.UID

/** Akka Imports **/
import akka.actor.{ActorSystem, Props}
import ayai.components._
import ayai.factories.EntityFactory

object QuestGenerationSystem {
  def apply(actorSystem: ActorSystem) = new QuestGenerationSystem(actorSystem)
}

class QuestGenerationSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Attack])) {
    def processEntity(e: Entity, deltaTime: Int) {
    }
}
