package ayai.systems

/**
 * ayai.system.CollisionSystem
 */

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.collisions._
import ayai.gamestate.{RoomWorld, TileMap}
import ayai.networking.ConnectionWrite

/** Crane Imports **/
import crane.{Entity, System, World}

/** External Imports **/
import scala.math.abs
import scala.util.control.Breaks._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._

import org.slf4j.{Logger, LoggerFactory}
object CollisionSystem {
  def apply(actorSystem: ActorSystem) = new CollisionSystem(actorSystem)
}

class CollisionSystem(actorSystem: ActorSystem) extends System {
  private val log = LoggerFactory.getLogger(getClass)

  def valueInRange(value: Int, min: Int, max: Int): Boolean = (value >= min) && (value <= max)

  def handleAttack(entityA: Entity, entityB: Entity):Boolean = {
    (entityA.getComponent(classOf[Attack]),
      entityB.getComponent(classOf[Attack]),
      entityA.getComponent(classOf[Health]),
      entityB.getComponent(classOf[Health])) match {
      case(Some(attackComponentA : Attack), None, None, Some(healthComponentB : Health)) =>
          if(attackComponentA.initiator != entityB) {
            attackComponentA.addVictim(entityB)
          }
          true
      case (None, Some(attackComponentB : Attack), Some(healthComponentA : Health), None) =>
          if(attackComponentB.initiator != entityA) {
            attackComponentB.addVictim(entityA)
          }
          true
      case _ => false
      }
  }

  def handleCollision(entityA: Entity, entityB: Entity) {
      (entityA.getComponent(classOf[Position]),
        entityA.getComponent(classOf[Bounds]),
        entityB.getComponent(classOf[Position]),
        entityB.getComponent(classOf[Bounds])) match {
        case(Some(positionA: Position), Some(boundsA: Bounds), Some(positionB: Position), Some(boundsB: Bounds)) =>
          // Remember to end a line with an operator of some sort (., +, &&, ||) if you need
          // to not fall afoul of the automatic end of statement guesser
          // are the two characters within the same x position (is A between B's leftside and width length)
          val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                                  valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

          //are the two characters within the same height area.
          val yOverlap: Boolean = valueInRange(positionA.y, positionB.y, positionB.y + boundsB.height) ||
                                  valueInRange(positionB.y, positionA.y, positionA.y + boundsA.height)

          if(xOverlap && yOverlap) {
            if (handleAttack(entityA, entityB)) {
              return // EXPLICIT RETURN TO ESCAPE handleCollision
            }
            // // check to see if they are movable
            // if(abs(positionA.y - positionB.y) < abs(positionA.x - positionB.x)) {
            //   if(positionA.x < positionB.x) {
            //     LeftDirection.process(entityA)
            //     RightDirection.process(entityB)
            //   } else {
            //     RightDirection.process(entityA)
            //     LeftDirection.process(entityB)
            //   }
            // } 
            // else {
            //   if(positionA.y < positionB.y) {
            //     UpDirection.process(entityA)
            //     DownDirection.process(entityB)
            //   } else {
            //     DownDirection.process(entityA)
            //     UpDirection.process(entityB)
            //   }
            // }
          }
        case _ => return
      }
  }


  override def process(delta: Int) {
    var entities = world.getEntitiesWithExclusions(include=List(classOf[Position], classOf[Bounds]),
                                                   exclude=List(classOf[Respawn], classOf[Transport], classOf[Dead]))
    var tileMap = world.asInstanceOf[RoomWorld].tileMap
    var quadTree: QuadTree = new QuadTree(0, new Rectangle(0,0,tileMap.maximumWidth, tileMap.maximumHeight))
    for(entity <- entities) {
      quadTree.insert(entity)
    }

    val quads = quadTree.quadrants
    for(quad <- quads) {
      while(quad.length > 1) {
        for(against <- quad.tail) {
          handleCollision(quad.head, against)
        }
        quad.remove(0)
      }
    }
  }

}
