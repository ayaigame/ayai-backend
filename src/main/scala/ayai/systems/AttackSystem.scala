package ayai.systems

import crane.{Component, Entity, World, EntityProcessingSystem}
import ayai.components._
import ayai.networking._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.rmi.server.UID
import ayai.factories._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object AttackSystem {
  def apply(actorSystem: ActorSystem): AttackSystem = new AttackSystem(actorSystem)
}

/**
** The attack system will only process on Attack components
** Will process all Attack components and will run damage functions on all attacked victims
** Does not attack members of own faction
**/
class AttackSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include = List(classOf[Attack])) {
  def processEntity(e: Entity, deltaTime: Int) {
    e.getComponent[Attack] match {
      case Some(attack: Attack) =>
      if (!attack.victims.isEmpty) {
        for(victim <- attack.victims) {
          if (!attack.attacked.contains(victim)) {
            val victimFaction = victim.getComponent[Faction] match {
              case Some(faction: Faction) => faction.name
              case _ => ""
            }
            val initiatorFaction = attack.initiator.getComponent[Faction] match {
              case Some(faction: Faction) => faction.name
              case _ => ""
            }
            if (victimFaction != initiatorFaction) {
              processAttack(attack.initiator, victim)
            }
          }
        }
        //moves victims to another array so they arent hit again
        attack.moveVictims()
      }
      case _ =>
    }

  }

  /**
  ** Return the amount of weapon damage the initator has
  **/
  def getWeaponStat(entity: Entity): Int = {
    var playerBase: Int = 5
    var weaponValue: Int = 5
    entity.getComponent[Stats] match {
      case Some(stats: Stats) =>
        for(stat <- stats.stats) {
          if (stat.attributeType == "strength"){
            playerBase = stat.magnitude
          }
        }
      case _ =>
    }
    entity.getComponent[Equipment] match {
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

  def handleAttackDamage(damage: Int, victim: Health): Unit = {
    victim.addDamage(damage)
  }

  /**
  ** Return armor value of an entity
  **/
  def getArmorStat(entity: Entity) : Int = {
    var playerBase : Int = 0
    var armorValue : Int = 0
    entity.getComponent[Stats] match {
      case Some(stats : Stats) =>
        for(stat <- stats.stats) {
          if (stat.attributeType == "defense"){
            playerBase += stat.magnitude
          }
        }
      case _ =>
    }
    armorValue + playerBase
  }

  /**
  ** I calculate the damage from an initiator to a victim.
  ** Then I apply the damage and check to see if the victim died.
  ** If it's dead then I award experience and set up the loot.
  ** Finally I notify all client connections of the attack.
  **/
  def processAttack(initiator: Entity, victim: Entity) {
    // calculate the attack
    var damage : Int = getWeaponStat(initiator)
    damage -= getArmorStat(victim)
    val healthComponent = victim.getComponent[Health] match {
      case Some(health : Health) => health
      case _ => null
    }
    // remove the attack component of entity A
    var damageDone = handleAttackDamage(damage, healthComponent)
    val initiatorId = initiator.getComponent[Character] match {
      case Some(character : Character) =>
        character.id
    }
    val victimId = victim.getComponent[Character] match {
      case Some(character : Character) => character.id
    }

    // attack message to be sent to all players to notify of attacked
    val attackMessage = ("type" -> "attack") ~
      ("damage" -> damage) ~
      ("initiator" -> initiatorId) ~
      ("victim" -> victimId)
    var actorSelection = actorSystem.actorSelection("user/SockoSender*")
    actorSelection ! new ConnectionWrite(compact(render(attackMessage)))

    val attackChat = ("type" -> "chat") ~
      ("message" -> ("Damage: " + damage.toString)) ~
      ("sender" -> "system")
    actorSelection = actorSystem.actorSelection("user/SockoSender*")
    actorSelection ! new ConnectionWrite(compact(render(attackChat)))

    var victimPosition = victim.getComponent[Position] match {
      case Some(position: Position) => position
      case _ => new Position(100,100)
    }
    //if the victim's health reaches zero, then take the persons inventory and make it lootable
    if (healthComponent.currentHealth <= 0) {
      victim.components += new Time(1000, System.currentTimeMillis())
      victim.components += new Dead()
      // get experience and add that to the initiator's experience
      (initiator.getComponent[Experience],
        victim.getComponent[Experience],
        initiator.getComponent[NPC],
        victim.getComponent[NPC]) match {
        case (Some(initiatorExperience: Experience), Some(victimExperience: Experience), None, Some(npc: NPC)) => 
          initiatorExperience.baseExperience += victimExperience.baseExperience
          initiator.getComponent[NetworkingActor] match {
            case Some(na: NetworkingActor) => 
              val att = ("type" -> "experience") ~
                        ("earned" -> victimExperience.baseExperience) 
              na.actor ! new ConnectionWrite(compact(render(att)))
              val ch =  ("type" -> "chat") ~
                        ("message" ->
                          ("earned: " + victimExperience.baseExperience.toString + " total experience now " + initiatorExperience.baseExperience.toString)) ~
                        ("sender" -> "system")
              na.actor ! new ConnectionWrite(compact(render(ch)))
            case _ =>
          }
        case _ =>
          //character should not get experience (usually NPCs (they have no need to level up))
      }

      val loot = EntityFactory.characterToLoot(world, victim, victimPosition)
      world.addEntity(loot)
      val json = ("type" -> "disconnect") ~
         ("id" -> victimId)
      val actorSelectionDisc = actorSystem.actorSelection("user/SockoSender*")
      actorSelectionDisc ! new ConnectionWrite(compact(render(json)))

    }
  }
}
