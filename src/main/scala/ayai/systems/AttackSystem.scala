package ayai.systems

import crane.{Component, Entity, World, EntityProcessingSystem}
import ayai.components._
import ayai.networking._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object AttackSystem {
  def apply(actorSystem: ActorSystem) = new AttackSystem(actorSystem)
}

class AttackSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[Attack])) {
  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent(classOf[Attack]) match {
      case Some(attack: Attack) =>  
      if(!attack.victims.isEmpty) {
        for(victim <- attack.victims) {
          getDamage(attack.initiator, victim)
        }
        attack.removeVictims()
      }
      case _ => 
    }
    
  }

  def getWeaponStat(entity: Entity): Int = {
    var playerBase: Int = 5
    var weaponValue: Int = 5
    entity.getComponent(classOf[Stats]) match {
      case Some(stats: Stats) =>
        for(stat <- stats.stats) {
          if(stat.attributeType == "strength"){
            playerBase += stat.magnitude
          }
        }
      case _ =>
    }
    entity.getComponent(classOf[Equipment]) match {
      case Some(equipment: Equipment) =>
          equipment.equipmentMap("weapon1").itemType match {
            case weapon: Weapon =>
            weaponValue += weapon.damage
            case _ =>
        }
          equipment.equipmentMap("weapon2").itemType match {
            case weapon: Weapon =>
            weaponValue += weapon.damage
            case _ =>
        }
    }
    weaponValue + playerBase
  }

  def handleAttackDamage(damage: Int, victim: Health) = {
    victim.currentHealth -= damage
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
    armorValue + playerBase
  }

  def getDamage(initiator: Entity, victim: Entity) {
    // calculate the attack
    var damage : Int = getWeaponStat(initiator)
    damage -= getArmorStat(victim)
    val healthComponent = victim.getComponent(classOf[Health]) match {
      case Some(health : Health) => health
      case _ => null
    }
    // remove the attack component of entity A
    var damageDone = handleAttackDamage(damage, healthComponent)
    val initiatorId = initiator.getComponent(classOf[Character]) match {
      case Some(character : Character) =>
        character.id
    }
    val victimId = victim.getComponent(classOf[Character]) match {
      case Some(character : Character) => character.id
    }


    //if the victims health reaches zero, then take the persons inventory and make it lootable
    if(healthComponent.currentHealth <= 0) {
      val loot:Entity = world.createEntity("LOOT"+initiatorId)
      loot.components += new Loot(initiatorId)
      val inventory = initiator.getComponent(classOf[Inventory]) match {
        case (Some(inv: Inventory)) =>
          inv.copy()
        case _ => null
      }
      loot.components += inventory
    }

    val att = ("type" -> "attack") ~
      ("damage" -> damage) ~
      ("initiator" -> initiatorId) ~
      ("victim" -> victimId)
    val actorSelection = actorSystem.actorSelection("user/SockoSender*")
    actorSelection ! new ConnectionWrite(compact(render(att)))
  } 
} 