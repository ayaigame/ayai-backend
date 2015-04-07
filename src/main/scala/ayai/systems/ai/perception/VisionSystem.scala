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

class VisionSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Vision])) {
  private val log = LoggerFactory.getLogger(getClass)
  private val spamLog = false

  override def processEntity(e: Entity, deltaTime: Int): Unit = {

    var allEntities = world.getEntitiesWithExclusions(include=List(classOf[Position], classOf[Bounds]),
      exclude=List(classOf[Respawn], classOf[Transport], classOf[Dead]))
    val tileMap = world.asInstanceOf[RoomWorld].tileMap

    (e.getComponent(classOf[Position]),
      e.getComponent(classOf[Bounds]), e.getComponent(classOf[Vision]), e.getComponent(classOf[Actionable])) match {
      case (Some(position: Position), Some(bounds: Bounds), Some(vision: Vision), Some(actionable: Actionable)) => {
        if ( actionable.active) {
          for (entity <- allEntities) {
            (entity.getComponent(classOf[Position])) match {
              case (Some(position2: Position)) => {

                if (position != position2) {
                  var entity1LOS = vision.drawLine(position, position2, bounds, tileMap)
                  var entity2LOS = vision.drawLine(position2, position, bounds, tileMap)

                  if (entity1LOS == true) {

                    (e.getComponent(classOf[Character]),
                      entity.getComponent(classOf[Character])) match {
                      case (Some(char1: Character), Some(char2: Character)) => {

                        if (spamLog) log.warn(char1.name + " sees " + char2.name)
                      }
                    }
                  }
                  if (entity2LOS == true) {

                    (e.getComponent(classOf[Character]),
                      entity.getComponent(classOf[Character])) match {
                      case (Some(char1: Character), Some(char2: Character)) => {

                        if (spamLog) log.warn(char2.name + " sees " + char1.name)
                      }

                    }
                  }
                }
              }
            }
          }
        }
      }

      // vision.seen? See AttackSystem, namely attack.victims comparison

    }
  }

  def getDistance(p1: Position, p2 : Position): Int = {
    var result = 0
    result += math.abs(p1.x - p2.x)
    result += math.abs(p1.y - p2.y)
    result
  }
}