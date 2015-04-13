package ayai.systems

import ayai.actions.AttackAction
import ayai.components._
import ayai.components.SoundEntity

import akka.actor.ActorSystem

import crane.{Entity, EntityProcessingSystem}

import org.slf4j.{Logger, LoggerFactory}

object SoundSystem {
  def apply(actorSystem: ActorSystem) = new SoundSystem(actorSystem)
}

class SoundSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Hearing])) {
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false

  override def processEntity(e: Entity, deltaTime: Int): Unit = {
    (e.getComponent(classOf[Position]), e.getComponent(classOf[Bounds]),
      e.getComponent(classOf[SoundProducing]), e.getComponent(classOf[Actionable])) match {
      case (Some(soundPosition: Position), Some(bounds: Bounds), Some(soundProd: SoundProducing), Some(actionable: Actionable)) => {
        if (actionable.active) {

          val soundEntity = new SoundEntity(soundProd.intensity, soundPosition)

          val hearingEntities = world.getEntitiesWithExclusions(include=List(classOf[Position], classOf[Bounds], classOf[Hearing]),
            exclude=List(classOf[Respawn], classOf[Transport], classOf[Dead]))

          for (entity <- hearingEntities) {
            (entity.getComponent(classOf[Position]), entity.getComponent(classOf[Hearing])) match {
              case (Some(hearPosition: Position), Some(hearing : Hearing)) => {

                if (e != entity && (soundEntity.intensity * hearing.hearingAbility) > getDistance(soundEntity.origin, hearPosition)) {

                  (e.getComponent(classOf[Character]),
                    entity.getComponent(classOf[Character])) match {
                    case (Some(char1: Character), Some(char2: Character)) => {

                      if (spamLog) log.warn(char2.name + " hears " + char1.name)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  def calculateSoundPropagation(soundEntity: Entity) {

  }

  def getDistance(p1: Position, p2: Position): Int = {
    // Manhattan distance
    math.abs(p1.x - p2.x) + math.abs(p1.y - p2.y)
  }
}
