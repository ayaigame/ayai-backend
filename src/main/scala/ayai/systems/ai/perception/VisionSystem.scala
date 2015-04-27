package ayai.systems

import ayai.components._
import ayai.gamestate._

import crane.{Entity, EntityProcessingSystem}

import akka.actor.ActorSystem

import scala.collection.mutable.ArrayBuffer

import org.slf4j.{Logger, LoggerFactory}

object VisionSystem {
  def apply(actorSystem: ActorSystem) = new VisionSystem(actorSystem)
}

class VisionSystem(actorSystem: ActorSystem) extends PerceptionSystem(actorSystem, include=List(classOf[Vision])) {
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false
  private val TICKS_BETWEEN_PROCESSING = 10

  private var counter = 0

  override def processEntity(e: Entity, deltaTime: Int): Unit = {
    if (counter >= TICKS_BETWEEN_PROCESSING) {
      var seeingEntity = e
      (seeingEntity.getComponent(classOf[Position]),
        seeingEntity.getComponent(classOf[Bounds]), seeingEntity.getComponent(classOf[Vision]), seeingEntity.getComponent(classOf[Actionable])) match {
        case (Some(position: Position), Some(bounds: Bounds), Some(vision: Vision), Some(actionable: Actionable)) => {
          if (actionable.active) {

            val allEntities = world.getEntitiesWithExclusions(include = List(classOf[Position], classOf[Bounds]),
              exclude = List(classOf[Respawn], classOf[Transport], classOf[Dead], classOf[Attack]))
            val tileMap = world.asInstanceOf[RoomWorld].tileMap

            for (seenEntity <- allEntities) {
              (seenEntity.getComponent(classOf[Position])) match {
                case (Some(position2: Position)) => {

                  if (position != position2) {
                    val entity1LOS = vision.drawLine(position, position2, bounds, tileMap)
                    val entity2LOS = vision.drawLine(position2, position, bounds, tileMap)

                    if (entity1LOS == true && (vision.visionRange > getDistance(position, position2))) {

                      (seeingEntity.getComponent(classOf[Character]),
                        seenEntity.getComponent(classOf[Character])) match {
                        case (Some(char1: Character), Some(char2: Character)) => {
                          if (spamLog) log.warn(char1.name + " sees " + char2.name)
                        }
                        case _ =>
                      }
                    }
                    (seenEntity.getComponent(classOf[Vision])) match {
                      case (Some(vision2: Vision)) => {
                        if (entity2LOS == true && (vision2.visionRange > getDistance(position, position2))) {

                          (seeingEntity.getComponent(classOf[Character]),
                            seenEntity.getComponent(classOf[Character])) match {
                            case (Some(char1: Character), Some(char2: Character)) => {

                              if (spamLog) log.warn(char2.name + " sees " + char1.name)

                            }
                            case _ =>
                          }
                        }
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

  def getDistance(p1: Position, p2: Position): Int = {
    // Manhattan distance
    math.abs(p1.x - p2.x) + math.abs(p1.y - p2.y)
  }
}
