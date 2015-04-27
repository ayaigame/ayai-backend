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

class SoundSystem(actorSystem: ActorSystem) extends PerceptionSystem(actorSystem, include=List(classOf[Hearing])) {
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false
  private val TICKS_BETWEEN_PROCESSING = 10

  private var counter = 0

  override def processEntity(e: Entity, deltaTime: Int): Unit = {
    if (counter == TICKS_BETWEEN_PROCESSING) {
      var soundProducingEntity = e
      (soundProducingEntity.getComponent(classOf[Position]), soundProducingEntity.getComponent(classOf[Bounds]),
        soundProducingEntity.getComponent(classOf[SoundProducing]), soundProducingEntity.getComponent(classOf[Actionable])) match {
        case (Some(soundPosition: Position), Some(bounds: Bounds), Some(soundProd: SoundProducing), Some(actionable: Actionable)) => {
          if (actionable.active) {

            val soundEntity = new SoundEntity(soundProd.intensity, soundPosition)

            val hearingEntities = world.getEntitiesWithExclusions(include = List(classOf[Position], classOf[Bounds], classOf[Hearing]),
              exclude = List(classOf[Respawn], classOf[Transport], classOf[Dead]))

            for (hearingEntity <- hearingEntities) {
              (hearingEntity.getComponent(classOf[Position]), hearingEntity.getComponent(classOf[Hearing])) match {
                case (Some(hearPosition: Position), Some(hearing: Hearing)) => {

                  if (soundProducingEntity != hearingEntity && (soundEntity.intensity * hearing.hearingAbility) > getDistance(soundEntity.origin, hearPosition)) {

                    (soundProducingEntity.getComponent(classOf[Character]),
                      hearingEntity.getComponent(classOf[Character])) match {
                      case (Some(char1: Character), Some(char2: Character)) => {

                        if (spamLog) log.warn(char2.name + " hears " + char1.name)

                      }
                      case _ =>
                    }
                  }
                }
                case _ =>
              }
            }
          }
        }
        case _ =>
      }
      counter = 0
    }
    else {
      counter+=1
    }
  }

  def calculateSoundPropagation(soundEntity: Entity) {

  }

  def getDistance(p1: Position, p2: Position): Int = {
    // Manhattan distance
    math.abs(p1.x - p2.x) + math.abs(p1.y - p2.y)
  }
}
