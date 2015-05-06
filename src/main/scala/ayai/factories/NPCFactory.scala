package ayai.factories

import ayai.components._
import crane.{World, Component, Entity}
import ayai.gamestate._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._
import ayai.apps._ 

case class AllNPCValues(
  id: Int,
  name: String,
  roomId: Int,
  faction: String,
  level: Int,
  experience: Long,
  maximumHealth: Int,
  maximumMana: Int,
  xposition: Int,
  yposition: Int,
  quests: QuestValue,
  weapon1: Int,
  helmet: Int,
  torso: Int,
  legs: Int,
  feet: Int
)
case class QuestValue(questId: Int)
case class InventoryValue(itemId: Int)
case class EquipmentValues(weapon1: Int, helmet: Int, torso: Int, legs: Int, feet: Int)

object NPCFactory {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  def bootup(networkSystem: ActorSystem) = {
    val npcs: List[AllNPCValues] = getNPCList("src/main/resources/npcs/npcs.json")
    val roomList = networkSystem.actorSelection("user/RoomList")
    val questMap = networkSystem.actorSelection("user/QuestMap")
    val itemMap = networkSystem.actorSelection("user/ItemMap")
    for (npc <- npcs) {
      println(s"Loading NPC ID = ${npc.id}, name = ${npc.name}")

      val future = roomList ? GetWorldById(npc.roomId)
      val roomWorld = Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]] match {
        case Some(room: RoomWorld) => room
        case _ => null
      }

      val ent: Entity = EntityFactory.createNPC(roomWorld, npc.faction, npc)
      val questfuture =  questMap ? GetQuest("QUEST"+npc.quests.questId)
      Await.result(questfuture, timeout.duration).asInstanceOf[Quest] match {
        case quest: Quest => 
          val questBag = new QuestBag()
          questBag.addQuest(quest)
          ent.components += questBag
        case _ => 
      }

      val equipment: Equipment = new Equipment()
      var itemFuture = itemMap ? GetItem(npc.weapon1.toString)
      var itemResult = Await.result(itemFuture, timeout.duration).asInstanceOf[Item] match {
        case item: Item => item
        case _ => new EmptySlot("weapon1")
      }
      equipment.equipItem(itemResult)
      itemFuture = itemMap ? GetItem(npc.feet.toString)
      itemResult = Await.result(itemFuture, timeout.duration).asInstanceOf[Item] match {
        case item: Item => item
        case _ => new EmptySlot("feet")
      }
      equipment.equipItem(itemResult)
      itemFuture = itemMap ? GetItem(npc.torso.toString)
      itemResult = Await.result(itemFuture, timeout.duration).asInstanceOf[Item] match {
        case item: Item => item
        case _ => new EmptySlot("torso")
      }
      equipment.equipItem(itemResult)
      itemFuture = itemMap ? GetItem(npc.helmet.toString)
      itemResult = Await.result(itemFuture, timeout.duration).asInstanceOf[Item] match {
        case item: Item => item
        case _ => new EmptySlot("helmet") 
      }
      equipment.equipItem(itemResult)
      itemFuture = itemMap ? GetItem(npc.legs.toString)
      itemResult = Await.result(itemFuture, timeout.duration).asInstanceOf[Item] match {
        case item: Item => item
        case _ => new EmptySlot("legs") 
      }
      equipment.equipItem(itemResult)
      ent.components += equipment
      roomWorld.addEntity(ent)

      // also save npc data to map to be updated
      networkSystem.actorSelection("user/NPCMap") ! AddNPC(npc.id.toString, new NPCValues(npc.id, npc.name,
        npc.faction, npc.roomId, npc.weapon1, npc.torso, npc.legs, npc.helmet, npc.feet, npc.level, npc.experience, npc.maximumHealth, 
        npc.maximumMana, npc.xposition, npc.yposition))
    }
  }

  def getNPCList(path: String): List[AllNPCValues] = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    val rootClasses = (parsedJson \\ "npcs").extract[List[AllNPCValues]]

    // val listOfLists: List[List[Quest]] = rootClasses.map((path: String) => getClassesList(path))
    
    rootClasses
    // listOfLists.foreach(e => questList.appendAll(e))
    // questList.toList
  }
}