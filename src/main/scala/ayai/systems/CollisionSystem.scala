package ayai.systems

/** 
 * ayai.system.CollisionSystem
 */

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.collisions._
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

class CollisionSystem(actorSystem : ActorSystem) extends System {
  private val log = LoggerFactory.getLogger(getClass)

  def valueInRange(value: Int, min: Int, max: Int): Boolean = {
    return (value >= min) && (value <= max)
  }


  //Eventually create a damage system to calculate this based on the users
  //equipped item
  def handleAttackDamage(damage : Int, attackee: Health) {
    val currentHealth = attackee.getCurrentHealth
    println("Damage Detected!")
    attackee.setCurrentHealth(currentHealth - damage)
  }

  def getWeaponStat(entity : Entity) : Int = {
    var playerBase : Int = 0
    var weaponValue : Int = 0
    entity.getComponent(classOf[Stats]) match {
      case Some(stats : Stats) => 
        for(stat <- stats.stats) {
          if(stat.attributeType == "strength"){
            playerBase += stat.magnitude
          }
        }
      case _ =>
    }
    entity.getComponent(classOf[Equipment]) match {
      case Some(equipment : Equipment) =>
        if(equipment.weapon1 != null) {
          equipment.weapon1.itemType match {
            case weapon : Weapon => 
            weaponValue += weapon.damage
            case _ => 
          }
        }
        if(equipment.weapon2 != null) {
          equipment.weapon2.itemType match {
            case weapon : Weapon => 
            weaponValue += weapon.damage
            case _ =>
          }
        }
    }
    return weaponValue + playerBase
  }

  def getArmorStat(entity: Entity) : Int = {
    var playerBase : Int = 0
    var armorValue : Int = 0
    entity.getComponent(classOf[Stats]) match {
      case Some(stats : Stats) => 
        for(stat <- stats.stats) {
          if(stat.attributeType == "defense"){
            playerBase += stat.magnitude
          }
        }
      case _ =>
    }
    return armorValue + playerBase
  }

  def getDamage(initiator: Entity, victim: Entity) {
        //calculate the attack
    var damage : Int = getWeaponStat(initiator)
    damage -= getArmorStat(victim)
    val healthComponent = victim.getComponent(classOf[Health]) match {
      case Some(health : Health) => health
      case _ => null
    }
    //remove the attack component of entity A
    var damageDone = handleAttackDamage(damage, healthComponent)
    val initiatorId = initiator.getComponent(classOf[Character]) match {
      case Some(character : Character) =>
        character.id 
    }
    val victimId = victim.getComponent(classOf[Character]) match {
      case Some(character : Character) => character.id
    }
  
    val att = ("attack" -> 
      ("damage" -> damage) ~
      ("initiator" -> initiatorId) ~
      ("victim" -> victimId))
    val actorSelection = actorSystem.actorSelection("user/SockoSender*")
    actorSelection ! new ConnectionWrite(compact(render(att))) 
  }

  def handleAttack(entityA: Entity, entityB: Entity):Boolean = {
    (entityA.getComponent(classOf[Attack]),
      entityB.getComponent(classOf[Attack]),
      entityA.getComponent(classOf[Health]),
      entityB.getComponent(classOf[Health])) match {
      case(Some(attackComponentA : Attack), None, None, Some(healthComponentB : Health)) =>
          //calculate the attack
          getDamage(attackComponentA.initiator, entityB)
          entityA.kill()
          entityA.components += new Dead()
          true
      case (None, Some(attackComponentB : Attack), Some(healthComponentA : Health), None) =>
          getDamage(attackComponentB.initiator, entityA)
          entityB.kill()
          entityB.components += new Dead()
          true
      case _ => false
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
            //check to see if they are movable
            if(abs(positionA.y - positionB.y) < abs(positionA.x - positionB.x)) {
              if(positionA.x < positionB.x) {
                LeftDirection.process(entityA)
                RightDirection.process(entityB)
              } else {
                RightDirection.process(entityA)
                LeftDirection.process(entityB)
              }
            } else {
              if(positionA.y < positionB.y) {
                UpDirection.process(entityA)
                DownDirection.process(entityB)
              } else {
                DownDirection.process(entityA)
                UpDirection.process(entityB)
              }
            }
          }
        case _ => return
      }
  }


  override def process(delta : Int) {
    //for each room get entities
    val exclusion = List(classOf[Respawn], classOf[Transport], classOf[Dead])
    val rooms : ArrayBuffer[Entity] = world.groups("ROOMS")
    for(e <- rooms) {
      // This needs to warn us better
      val tileMap = (e.getComponent(classOf[TileMap]): @unchecked) match {
        case(Some(tilemap : TileMap)) => 
          tilemap
      }
      val room = (e.getComponent(classOf[Room])) match {
        case (Some(r : Room)) => r
        case _ =>
          log.warn("0b38a4a: getComponent failed to return anything")
          new Room(-1)
      }
      
      var quadTree : QuadTree = new QuadTree(0, new Rectangle(0,0,tileMap.getMaximumWidth, tileMap.getMaximumHeight))
      var entities = world.groups("ROOM"+room.id).toList
      entities = excludeList(entities, exclusion)
      for(entity <- entities) {
        quadTree.insert(entity)
      }

      val quads = quadTree.quadrants
      for(quad <- quads) {
        while(quad.length > 1) {
          for(against <- quad.tail) {
            if(!hasExclusion(quad.head, exclusion)) {
              handleCollision(quad.head, against)
            }
          }
          quad.remove(0)
        }
      }
    }
  }

  def excludeList[T <: AnyRef](entities : List[Entity],exclusionList : List[T] ) : List[Entity] = {
    entities.filter{ entity =>
      val componentEntityTypes : Set[Object] = entity.components.map(c=>c.getClass).toSet 
      (exclusionList.toSet intersect componentEntityTypes).isEmpty 
    }.toList

  }

  def hasExclusion[T <: AnyRef](entity : Entity, exclusionList : List[T]) : Boolean = {
      val componentEntityTypes : Set[Object] = entity.components.map(c=>c.getClass).toSet 
      !(exclusionList.toSet intersect componentEntityTypes).isEmpty
    }
}
