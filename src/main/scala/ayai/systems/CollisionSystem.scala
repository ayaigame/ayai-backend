package ayai.systems

/** 
 * ayai.system.CollisionSystem
 */

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.collisions._

/** Artemis Imports **/
import crane.{Entity, System, World}

import scala.collection.mutable.ArrayBuffer

import scala.collection.mutable.HashMap

/** External Imports **/
import scala.math.abs
import scala.util.control.Breaks._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._

class CollisionSystem(world: World) extends System {

  def valueInRange(value: Int, min: Int, max: Int): Boolean = {
    return (value >= min) && (value <= max)
  }


  //Eventually create a damage system to calculate this based on the users
  //equipped item
  def handleAttackDamage(attacker: Attack, attackee: Health) {
    val currentHealth = attackee.getCurrentHealth
    println("Damage Detected!")
    attackee.setCurrentHealth(currentHealth - attacker.damage)

    
  }

  def handleAttack(entityA: Entity, entityB: Entity):Boolean = {
    (entityA.getComponent(classOf[Attack]),
      entityB.getComponent(classOf[Attack]),
      entityA.getComponent(classOf[Health]),
      entityB.getComponent(classOf[Health])) match {
      case(Some(attackComponentA : Attack), Some(attackComponentB : Attack), Some(healthComponentA : Health), Some(healthComponentB : Health)) =>
        println("Attack collision detected!")
        implicit val formats = Serialization.formats(NoTypeHints)
        println(write(healthComponentA))
        println(write(attackComponentB))
        
        if (attackComponentA != null && healthComponentB != null) {
          handleAttackDamage(attackComponentA, healthComponentB)
          world.removeEntity(entityA)
          true     
        } else if (attackComponentB != null && healthComponentA != null) {
          handleAttackDamage(attackComponentB, healthComponentA)
          world.removeEntity(entityB)
          true    
        } else 
            false
      }
  }

  def handleCollision(entityA: Entity, entityB: Entity) {
      (entityA.getComponent(classOf[Position]),
        entityA.getComponent(classOf[Bounds]),
        entityB.getComponent(classOf[Position]),
        entityB.getComponent(classOf[Bounds])) match {
        case(Some(positionA : Position), Some(boundsA : Bounds), Some(positionB : Position), Some(boundsB : Bounds)) =>
          // Remember to end a line with an operator of some sort (., +, &&, ||) if you need 
          // to not fall afoul of the automatic end of statement guesser
          //are the two characters within the same x position (is A between B's leftside and width length) 
          val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                                  valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

          //are the two characters within the same height area.
          val yOverlap: Boolean = valueInRange(positionA.y, positionB.y, positionB.y + boundsB.height) ||
                                  valueInRange(positionB.y, positionA.y, positionA.y + boundsA.height)

          if(xOverlap && yOverlap) {
            if (handleAttack(entityA, entityB)) {
              return;

            }
            if(abs(positionA.y - positionB.y) < abs(positionA.x - positionB.x)) {
              if(positionA.x < positionB.x) {
                new MovementAction(new LeftDirection).process(entityA)
                new MovementAction(new RightDirection).process(entityB)
              } else {
                new MovementAction(new RightDirection).process(entityA)
                new MovementAction(new LeftDirection).process(entityB)
              }
            } else {
              if(positionA.y < positionB.y) {
                new MovementAction(new UpDirection).process(entityA)
                new MovementAction(new DownDirection).process(entityB)
              } else {
                new MovementAction(new DownDirection).process(entityA)
                new MovementAction(new UpDirection).process(entityB)
              }
            }
          }
      }
  }

  def handleBulletCollision(entityA: Entity, entityB: Entity) {
      (entityA.getComponent(classOf[Position]),
        entityA.getComponent(classOf[Bounds]),
        entityB.getComponent(classOf[Position]),
        entityB.getComponent(classOf[Bounds])) match {
        case(Some(positionA : Position), Some(boundsA : Bounds), Some(positionB : Position), Some(boundsB : Bounds)) =>
          // Remember to end a line with an operator of some sort (., +, &&, ||) if you need 
          // to not fall afoul of the automatic end of statement guesser
          //are the two characters within the same x position (is A between B's leftside and width length) 
          val xOverlap: Boolean = valueInRange(positionA.x, positionB.x, positionB.x + boundsB.width) ||
                                  valueInRange(positionB.x, positionA.x, positionA.x + boundsA.width)

          //are the two characters within the same height area.
          val yOverlap: Boolean = valueInRange(positionA.y, positionB.y, positionB.y + boundsB.height) ||
                                  valueInRange(positionB.y, positionA.y, positionA.y + boundsA.height)

          if(xOverlap && yOverlap) {
            (entityA.getComponent(classOf[Health]),
              entityB.getComponent(classOf[Bullet])) match {
                case(Some(health : Health), Some(bullet : Bullet)) => 
                  health.currentHealth -= bullet.damage
              }
            world.removeEntity(entityB)
          }
      }
  }

  override def process(delta : Int) {
    //for each room get entities
    val rooms : ArrayBuffer[Entity] = world.groups("ROOMS")
    for(e <- rooms) {
      val tileMap = (e.getComponent(classOf[TileMap])) match {
        case(Some(tilemap : TileMap)) => tilemap
      }
      val room = (e.getComponent(classOf[Room])) match {
        case (Some(r : Room)) => r
      }
      
      var quadTree : QuadTree = new QuadTree(0, new Rectangle(0,0,tileMap.getMaximumWidth, tileMap.getMaximumHeight))
      val entities : ArrayBuffer[Entity] = world.groups("ROOM"+room.id)
      for(entity <- entities) {
        quadTree.insert(entity)
      }

      for(entity <- entities) {
        var returnObjects : ArrayBuffer[Entity] = new ArrayBuffer[Entity]()
        quadTree.retrieve(returnObjects, entity)
        for(r <- returnObjects) {
          if(r != entity) {
            handleCollision(entity, r)
          }
        }
      }
    }
  }
}
