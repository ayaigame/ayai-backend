package ayai.gamestate

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.networking._
import ayai.networking.chat._
import ayai.persistence.{CharacterTable, InventoryTable}//Just testing this table
import ayai.factories.EntityFactory
import ayai.apps.{Constants, GameLoop}

/** Crane Imports **/
import crane.{Entity}

/** Akka Imports **/
import akka.actor.{Actor, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

/** External Imports **/
import scala.util.Random
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Promise, Future}
import scala.math._

import java.rmi.server.UID

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

import org.slf4j.{Logger, LoggerFactory}

object MessageProcessor {
  implicit val timeout = Timeout(2 seconds)
  import ExecutionContext.Implicits.global


  def apply(world: RoomWorld) = new MessageProcessor(world)
}

class MessageProcessor(world: RoomWorld) extends Actor {
  implicit val formats = Serialization.formats(NoTypeHints)
  implicit val timeout = Timeout(2 seconds)
  val actorSystem = context.system
  private val log = LoggerFactory.getLogger(getClass)

  def processMessage(message: Message) {
    message match {
      //Should  take characterId: Long as a parameter instead of characterName
      //However can't do that until front end actually gives me the characterId
      case AddNewCharacter(id: String, characterName: String, x: Int, y: Int) => {
        val actor = actorSystem.actorSelection(s"user/SockoSender$id")

        EntityFactory.loadCharacter(world, id, characterName, x, y, actor, actorSystem)
        sender ! Success
      }

      case RemoveCharacter(socketId: String) => {
        val future = context.system.actorSelection("user/SocketUserMap") ? GetUserId(socketId)
        val userId = Await.result(future, timeout.duration).asInstanceOf[String]
        world.getEntityByTag(s"$userId") match {
          case None =>
            System.out.println(s"Can't find character attached to socket $socketId.")
          case Some(character : Entity) =>
            CharacterTable.saveCharacter(character)
            InventoryTable.saveInventory(character)
            character.kill
            context.system.actorSelection("user/SocketUserMap") ! RemoveSocketUser(socketId)
        }
        sender ! Success
      }

      case MoveMessage(userId: String, start: Boolean, direction: MoveDirection) => {
        (world.getEntityByTag(s"$userId")) match {
          case None =>
            println(s"Can't find character attached to id: $userId")
          case Some(e: Entity) =>
              val oldMovement = (e.getComponent(classOf[Actionable])) match {
                case Some(oldMove : Actionable) =>
                  oldMove.active = start
                  oldMove.action = direction
                case _ =>
                  log.warn("a07270d: getComponent failed to return anything")
              }
        }
        sender ! Success
      }

       // give id of the item, and what action it should do (equip, use, unequip, remove from inventory)
      case AttackMessage(userId: String) => {
        //create a projectile

        val bulletId = (new UID()).toString

        (world.getEntityByTag(s"$userId")) match {

        case Some(initiator: Entity) =>
          (initiator.getComponent(classOf[Position]),
          initiator.getComponent(classOf[Actionable]),
          initiator.getComponent(classOf[Character]),
          initiator.getComponent(classOf[Room]),
          initiator.getComponent(classOf[Cooldown]),
          initiator.getComponent(classOf[Dead])) match {
            case(Some(pos: Position), Some(a: Actionable), Some(c: Character), Some(r: Room), None, None) =>

              val m = a.action match {
                case (move: MoveDirection) => move
                case _ =>
                  println("Not match for movedirection")
                  new MoveDirection(0, 0)
              }

              //get the range of the characters weapon
              val weaponRange = initiator.getComponent(classOf[Equipment]) match {
                case Some(e: Equipment) => e.equipmentMap("weapon1").itemType match {
                  case weapon: Weapon => weapon.range
                  case _ => 30
                }
                case _ => 30
              }

              val upperLeftx = pos.x
              val upperLefty = pos.y

              val xDirection = m.xDirection
              val yDirection = m.yDirection

              val topLeftOfAttackx = ((weaponRange) * xDirection) + upperLeftx
              val topLeftOfAttacky = ((weaponRange) * yDirection) + upperLefty


              val p: Entity = world.createEntity("ATTACK"+bulletId)

              p.components += (new Position(topLeftOfAttackx, topLeftOfAttacky))
              p.components += (new Bounds(weaponRange, weaponRange))
              p.components += (new Attack(initiator));
              p.components += (new Frame(30,0))


              initiator.components += (new Cooldown(System.currentTimeMillis(), 1000))

              //p.components += (c)
              world.addEntity(p)

            case _ =>
              // log.warn("424e244: Cooldown is present, cannot attack")
          }
          case _ =>
            log.warn("8a87265: getComponent failed to return anything")
      }
      sender ! Success
    }

      case ItemMessage(userId: String, itemAction: ItemAction) => {
        sender ! Success
      }

      case OpenMessage(userId: String, containerId : String) => {
        sender ! Success
      }

      case PublicChatMessage(message: String, sender: String) => {
        //// Will do this later - we don't have accounts working quite yet, so we will wait until that is ready
        //var sUser = None: Option[User]
        //sUser = UserQuery.getByUsername(sender)
        //
        //sUser match {
        //  case Some(user) =>
        //    val mh = new ChatHolder(new PublicChat(message, user))
        //    actorSystem.actorOf(Props(new ChatReceiver())) ! mh
        //  case _ =>
        //    println("Error from PublicChatMessage")
        //}
      }
      case EquipMessage(userId: String, slot: Int, equipmentType: String) =>
        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) =>
            (e.getComponent(classOf[Inventory]),
              e.getComponent(classOf[Equipment])) match {
                case (Some(inventory: Inventory), Some(equipment: Equipment)) =>
                  val item = inventory.inventory(slot)
                  val equipItem = equipment.equipmentMap(equipmentType)
                  if(equipment.equipItem(item)) {
                    inventory.removeItem(item)
                    if(!isEmptySlot(equipItem)) {
                      inventory.inventory += equipItem
                      println("Equip Item " + equipment.equipmentMap(equipmentType))
                    }
                    // sender ! Success
                  }
                  else {
                  }
                  InventoryTable.saveInventory(e)
              }
        }
        sender ! Success
      case UnequipMessage(userId: String, equipmentType: String) =>
        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) =>
            (e.getComponent(classOf[Inventory]),
              e.getComponent(classOf[Equipment])) match {
                case (Some(inventory: Inventory), Some(equipment: Equipment)) =>
                  val equippedItem = equipment.unequipItem(equipmentType)
                  equippedItem.itemType match {
                    case weapon: Weapon =>
                      inventory.inventory += equippedItem
                      equipment.equipmentMap(equipmentType) = new EmptySlot()
                    case armor: Armor =>
                      inventory.inventory += equippedItem
                      equipment.equipmentMap(equipmentType) = new EmptySlot()
                    case _ =>
                      println(equipmentType + " not valiid")
                  }
                case _ =>
                  println(s"User $userId cannot equip $equipmentType.")
              }
          case _ =>
            println(s"User $userId not found while unequiping.")
        }
        println("UnequipingMessage: " + equipmentType)
        sender ! Success

      case DropItemMessage(userId: String, slot: Int) =>
        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) =>
            (e.getComponent(classOf[Inventory])) match {
              case (Some(inventory: Inventory)) =>
                if(!(inventory.inventory.size <= 0)) {
                  //This should drop by item id not by slot
                  val item = inventory.inventory(slot)
                  inventory.inventory -= item
                  InventoryTable.deleteItem(item, e)
                }
            }
        }
        sender ! Success

      // copy quest information from npc to player
      // FIX NULLS IN MESSAGES
      case AcceptQuestMessage(userId: String, entityId: String, questId: Int) =>
        //might want to calculate interact message here
        // also might want to check distance between npc and player
        var npcQuest: Quest = world.getEntityByTag(s"$entityId") match {
          case Some(e: Entity) => e.getComponent(classOf[QuestBag]) match {
            case Some(questBag: QuestBag) =>
              var tempQuest: Quest = null
              for(quest <- questBag.quests) {
                if(quest.id == questId) {
                  tempQuest = quest
                }
              }
              tempQuest
            case _ => null
          }
          case _ => null
        }

        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) => e.getComponent(classOf[QuestBag]) match {
            case Some(questBag: QuestBag) =>
              questBag.addQuest(npcQuest)
          }
          case _ =>
        }
        sender ! Success
      case AbandonQuestMessage(userId: String, questId: Int) =>
        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) => e.getComponent(classOf[QuestBag]) match {
            case Some(questBag: QuestBag) =>
              for(quest <- questBag.quests) {
                if(quest.id == questId) {
                  questBag.quests -= quest
                  // tell frontend that quest is removed
                }
              }
          }

        }
        sender ! Success
        //sends loot-open and quest-offer
      case InteractMessage(userId: String, entityId: String) =>
        val entity = world.getEntityByTag(s"$entityId") match {
          case Some(e: Entity) => e
        }
        val userEntity = world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) => e
        }
        val isValidDistance: Boolean = (userEntity.getComponent(classOf[Position]),
                                        entity.getComponent(classOf[Position])) match {
          case (Some(userPosition: Position), Some(entityPosition: Position)) =>
            sqrt(pow(userPosition.x-entityPosition.x,2)+pow(userPosition.y-entityPosition.y,2)) <= Constants.SPACE_FOR_INTERACTION
          case _ => false
        }
        val actorSelection = userEntity.getComponent(classOf[NetworkingActor]) match {
          case Some(na: NetworkingActor) =>
            na.actor
        }
        //if valid distance then pick up items
        if(isValidDistance) {
          val loot = entity.getComponent(classOf[Inventory]) match {
            case Some(inv: Inventory) => inv
            case _ => null
          }
          val playerInventory = userEntity.getComponent(classOf[Inventory]) match {
            case Some(inv: Inventory) => inv
            case _ => null
          }
          var typename: String = ""
          val jsonObject: JObject = (entity.getComponent(classOf[QuestBag]),
            entity.getComponent(classOf[Loot])) match {
              case (Some(quests: QuestBag), Some(loot: Loot)) =>
                typename = quests.typename
                quests.asJson
              case (Some(quests: QuestBag), None) =>
                typename = quests.typename
                //fix this for a list
                if(!quests.quests.isEmpty) {
                  ("quest" -> quests.quests(0).asJson) ~
                  ("entityId" -> entityId)
                } else {
                  quests.asJson
                }
              case ( None, Some(loot: Loot)) =>
                typename = loot.typename
                entity.getComponent(classOf[Inventory]) match {
                  case Some(inventory: Inventory) =>
                   inventory.asJson
                  case _ => null
                }

              case _ => null
          }

          if(jsonObject != null) {
            // check for quest and loot status on a player (will add more later)
            val json = ("type" -> typename)~jsonObject
            println(compact(render(json)))
            actorSelection ! ConnectionWrite(compact(render(json)))
          } else {
            val json = ("type" -> "chat") ~
              ("message" -> "Nothing to Send") ~
              ("sender" -> "error")
            actorSelection ! ConnectionWrite(compact(render(json)))
          }
        }
        else {
          val json = ("type" -> "chat") ~
            ("message" -> "Not within distance of item") ~
            ("sender" -> "error")
          //check if player is an actor
          actorSelection ! ConnectionWrite(compact(render(json)))
        }
        sender ! Success


      case LootMessage(userId: String, entityId: String, items: List[Int]) =>
        val userEntity = world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) => e
        }
        val itemEntity = world.getEntityByTag(s"$entityId") match {
          case Some(e: Entity) => e
        }

        val isValidDistance: Boolean = (userEntity.getComponent(classOf[Position]),
                                        itemEntity.getComponent(classOf[Position])) match {
          case (Some(userPosition: Position), Some(itemPosition:Position)) =>
            sqrt(pow(userPosition.x-itemPosition.x,2)+pow(userPosition.y-itemPosition.y,2)) <= Constants.SPACE_FOR_INTERACTION
          case _ => false
        }

        //if valid distance then pick up items
        if(isValidDistance) {
          val loot = itemEntity.getComponent(classOf[Inventory]) match {
            case Some(inv: Inventory) => inv
            case _ => null
          }
          val playerInventory = userEntity.getComponent(classOf[Inventory]) match {
            case Some(inv: Inventory) => inv
            case _ => null
          }


          //take item and put it in player inventory
          for(item <- loot.inventory) {
            if(items.contains(item.id)) {
              playerInventory.addItem(item)
              InventoryTable.incrementItem(item, userEntity)
              loot.inventory -= item
            }
          }

//          actorSelection ! ConnectionWrite(compact(render(json)))
        } else {
          val json = ("type" -> "chat") ~
            ("message" -> "Not withing distance of item") ~
            ("sender" -> "error")
          val actorSelection = userEntity.getComponent(classOf[NetworkingActor]) match {
            case Some(na: NetworkingActor) =>
              na.actor
          }
          actorSelection ! ConnectionWrite(compact(render(json)))

        }
        sender ! Success
      case CreateAIMessage =>
        val ai = EntityFactory.createAI(world, "axis")
        world.addEntity(ai)
        sender ! Success

      case _ => println("Error from MessageProcessor.")
    }
  }


  def isEmptySlot(item: Item): Boolean = {
    item.isInstanceOf[EmptySlot]
  }

  def receive = {
    case ProcessMessage(message) =>
      processMessage(message)
    case _ => println("Error: from interpreter.")
      sender ! Failure
  }
}
