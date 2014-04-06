package ayai.systems

import crane.{Component, Entity, EntityProcessingSystem}
import ayai.components._
import ayai.apps.Constants
import ayai.networking._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

object LevelingSystem {
  def apply(networkSystem: ActorSystem) = new LevelingSystem(networkSystem)
}

class LevelingSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Experience], 
                                                                                           classOf[Character],
                                                                                           classOf[NetworkingActor])) {
  def processEntity(entity: Entity, deltaTime: Int) {
    (entity.getComponent(classOf[Experience]),
      entity.getComponent(classOf[Character]),
      entity.getComponent(classOf[NetworkingActor])) match {
      case (Some(experience: Experience), Some(character: Character), Some(na: NetworkingActor)) => 
        var expThresh = Constants.EXPERIENCE_ARRAY(experience.level-1)
        var leveledUp = experience.levelUp(expThresh)
        if(leveledUp) {
          val json = ("type" -> "chat") ~
            ("message" -> "Leveled Up to level $experience.level") ~
            ("sender" -> "system")
          val actorSelection = na.actor
          actorSelection ! ConnectionWrite(compact(render(json)))
        }
      case _ =>
    }
    
  }
}