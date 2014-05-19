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
      level: Int,
      experience: Long,
      maximumHealth: Int,
      xposition: Int,
      yposition: Int,
      quests: QuestValue,
      inventory: List[InventoryValue])
case class QuestValue(questId: Int)
case class InventoryValue(itemId: Int)
case class EquipmentValues(weaponId: Int, helmetId: Int, torsoId: Int, legsId: Int, feetId: Int)

object NPCFactory {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  def bootup(networkSystem: ActorSystem) = {
    var npcs: List[AllNPCValues] = getNPCList("src/main/resources/npcs/npcs.json")
    val roomList = networkSystem.actorSelection("user/RoomList")
    val questMap = networkSystem.actorSelection("user/QuestMap")
    val itemMap = networkSystem.actorSelection("user/ItemMap")
    for(npc <- npcs) {
      val future = roomList ? GetWorldByName("room"+npc.roomId)
      val roomWorld = Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]] match {
        case Some(room: RoomWorld) => room
        case _ => null
      }
      val ent: Entity = EntityFactory.createNPC(roomWorld, "allies", npc)
      val questfuture =  questMap ? GetQuest("QUEST"+npc.quests.questId)
      Await.result(questfuture, timeout.duration).asInstanceOf[Quest] match {
        case quest: Quest => 
          val questBag = new QuestBag()
          questBag.addQuest(quest)
          ent.components += questBag
        case _ => 
      }

      val inventory: Inventory = new Inventory
      for(id <- npc.inventory) {
        val itemFuture = itemMap ? GetItem("ITEM"+id.itemId)
        val itemResult = Await.result(itemFuture, timeout.duration).asInstanceOf[Item] match {
          case item: Item => item
          case _ => null 
        }
        if(itemResult != null) {
          inventory.addItem(itemResult)
        }
      }
      ent.components += inventory
      roomWorld.addEntity(ent)
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
    
    var npcList = new ArrayBuffer[AllNPCValues]()
    npcList.appendAll(rootClasses)
    npcList.toList
    // listOfLists.foreach(e => questList.appendAll(e))
    // questList.toList
  }
}