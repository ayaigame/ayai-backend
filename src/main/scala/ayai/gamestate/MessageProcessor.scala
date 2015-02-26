package ayai.gamestate

/** Ayai Imports **/
import ayai.actions._
import ayai.components._
import ayai.networking._
import ayai.persistence.{CharacterTable, InventoryTable} //Just testing this table
import ayai.factories.{EntityFactory, SpriteSheetFactory}
import ayai.apps.Constants

/** Crane Imports **/
import crane.Entity

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

/** External Imports **/
import java.rmi.server.UID
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.math._


object MessageProcessor {
  implicit val timeout = Timeout(2 seconds)

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
            character.kill()
            context.system.actorSelection("user/SocketUserMap") ! RemoveSocketUser(socketId)
        }
        sender ! Success
      }

      case MoveMessage(userId: String, start: Boolean, direction: MoveDirection) => {
        world.getEntityByTag(userId) match {
          case None => println(s"Can't find character attached to id: $userId")
          case Some(e: Entity) => {
            val oldMovement = e.getComponent(classOf[Actionable]) match {
              case Some(oldMove: Actionable) => {
                oldMove.active = start
                if (start) {
                  oldMove.action = direction
                }
              }
              case _ => log.warn("a07270d: getComponent failed to return anything")
            }
          }
        }
        sender ! Success
      }

       // give id of the item, and what action it should do (equip, use, unequip, remove from inventory)
      case AttackMessage(userId: String) => {
       //create a projectile

        val bulletId = new UID().toString

        world.getEntityByTag(userId) match {
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

              //get the range and name of the character's weapon
              var weaponRange = 30
              var weaponName = ""

              initiator.getComponent(classOf[Equipment]) match {
                case Some(e: Equipment) =>
                  val item = e.equipmentMap("weapon1")
                  weaponName = item.name
                  item.itemType match {
                    case weapon: Weapon =>
                      weaponRange = weapon.range
                    case _ =>
                }
              }

              val upperLeftx = pos.x
              val upperLefty = pos.y

              val xDirection = m.xDirection
              val yDirection = m.yDirection

              val p = world.createEntity("ATTACK" + bulletId)

              p.components += new Attack(initiator)

              //If weapon range >= 100 it is ranged, so fire a projectile
              if (weaponRange >= 100) {
                p.components += new Projectile(bulletId)
                p.components += new Velocity(Constants.PROJECTILE_VELOCITY, Constants.PROJECTILE_VELOCITY)
                p.components += new Actionable(true, a.action)
                p.components += new Position(upperLeftx, upperLefty)
                p.components += new Bounds(20, 20)
                p.components += new Frame(weaponRange/Constants.PROJECTILE_VELOCITY)

                if (SpriteSheetFactory.hasSpriteSheet(weaponName))
                  p.components += SpriteSheetFactory.getSpriteSheet(weaponName, xDirection, yDirection)
              }
              else { //it's melee
                val topLeftOfAttackx = (weaponRange * xDirection) + upperLeftx
                val topLeftOfAttacky = (weaponRange * yDirection) + upperLefty

                p.components += new Position(topLeftOfAttackx, topLeftOfAttacky)
                p.components += new Bounds(weaponRange, weaponRange)
                p.components += new Frame(30)
              }

              initiator.components += new Cooldown(System.currentTimeMillis(), 1000)

              //p.components += (c)
              world.addEntity(p)

            case _ =>
              // log.warn("424e244: Cooldown is present, cannot attack")
          }
          case _ => log.warn("8a87265: getComponent failed to return anything")
      }

      sender ! Success
    }

      case ItemMessage(userId: String, itemAction: ItemAction) => sender ! Success

      case OpenMessage(userId: String, containerId : String) => sender ! Success

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
        world.getEntityByTag(userId) match {
          case Some(e: Entity) =>
            (e.getComponent(classOf[Inventory]),
              e.getComponent(classOf[Equipment])) match {
                case (Some(inventory: Inventory), Some(equipment: Equipment)) =>
                  val item = inventory.inventory(slot)
                  e.getComponent(classOf[NetworkingActor]) match {
                    case Some(na: NetworkingActor) =>
                      val json = ("type" -> "chat") ~
                        ("message" -> ("Equiping Item: " + item.name)) ~
                        ("sender" -> "system")
                      na.actor ! ConnectionWrite(compact(render(json)))
                    case _ =>

                  }
                  val equipItem = equipment.unequipItem(equipmentType)
                  e.getComponent(classOf[NetworkingActor]) match {
                    case Some(na: NetworkingActor) =>
                      val json = ("type" -> "chat") ~
                        ("message" -> ("Unequipped Item: " + equipItem.name)) ~
                        ("sender" -> "system")
                      na.actor ! ConnectionWrite(compact(render(json)))
                    case _ =>

                  }
                  if (equipment.equipItem(item)) {
                    inventory.removeItem(item)
                    equipItem.itemType match {
                      case empty: Weapon =>  inventory.inventory += equipItem
                      case armor: Armor => inventory.inventory += equipItem
                      case empty: EmptyType =>
                      case _ =>
                    }
                    // sender ! Success
                  }
                  else {
                    // if cannot equip then don't do anything as it is ineligible
                  }
                  sender ! Success
                  InventoryTable.saveInventory(e)
              }
        }
      case UnequipMessage(userId: String, equipmentType: String) =>
        world.getEntityByTag(userId) match {
          case Some(e: Entity) =>
            (e.getComponent(classOf[Inventory]),
              e.getComponent(classOf[Equipment])) match {
                case (Some(inventory: Inventory), Some(equipment: Equipment)) => {
                  val equippedItem = equipment.unequipItem(equipmentType)
                  equippedItem.itemType match {
                    case weapon: Weapon => {
                      inventory.inventory += equippedItem
                      equipment.equipmentMap(equipmentType) = new EmptySlot(equipmentType)
                    }
                    case armor: Armor => {
                      inventory.inventory += equippedItem
                      equipment.equipmentMap(equipmentType) = new EmptySlot(equipmentType)
                    }
                    case _ => println(equipmentType + " not valiid")
                  }
                }
                case _ => println(s"User $userId cannot equip $equipmentType.")
              }
          case _ => println(s"User $userId not found while unequiping.")
        }
        println("UnequipingMessage: " + equipmentType)
        sender ! Success

      case DropItemMessage(userId: String, slot: Int) =>
        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) =>
            e.getComponent(classOf[Inventory]) match {
              case (Some(inventory: Inventory)) =>
                if (!(inventory.inventory.size <= 0)) {
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
        val npcQuest: Quest = world.getEntityByTag(s"$entityId") match {
          case Some(e: Entity) => e.getComponent(classOf[QuestBag]) match {
            case Some(questBag: QuestBag) =>
              var tempQuest: Quest = null
              for(quest <- questBag.quests) {
                if (quest.id == questId) {
                  tempQuest = quest
                }
              }
              tempQuest
            case _ => null
          }
          case _ => null
        }

        world.getEntityByTag(userId) match {
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
                if (quest.id == questId) {
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
                if (!quests.quests.isEmpty) {
                  ("quest" -> quests.quests(0).asJson) ~
                  ("entityId" -> entityId)
                } else {
                  quests.asJson
                }
              case ( None, Some(loot: Loot)) =>
                typename = loot.typename
                entity.getComponent(classOf[Inventory]) match {
                  case Some(inventory: Inventory) =>
                   ("loot" -> loot.asJson)~
                   (inventory.asJson)
                  case _ => null
                }

              case _ => null
          }

          if (jsonObject != null) {
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


      case LootMessage(userId: String, entityId: String, itemId: Int) =>
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
            case _ =>  {
              println("Could not find inventory")
              new Inventory()
            }
          }

          val personInv = userEntity.getComponent(classOf[Inventory]) match {
            case Some(inv: Inventory) => inv
            case _ => {
              println("PlayerInventory could not be made")
              new Inventory()
            }
          }


          //take item and put it in player inventory
          var itemToRemove: Item  = null
          for(itemInv <- loot.inventory) {
            if (itemInv.id == itemId) {
              personInv.addItem(itemInv)
              InventoryTable.incrementItem(itemInv, userEntity)
              itemToRemove = itemInv
            }
          }
          if (itemToRemove != null) {
            loot.removeItem(itemToRemove)
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
        val ai = EntityFactory.createAI(world, "axis", actorSystem)
        world.addEntity(ai)
        sender ! Success

      case UseItemMessage(userId: String, itemId: Int) =>
      println("itemuse message")
        val itemUseId = (new UID()).toString
        world.getEntityByTag(s"$userId") match {
          case Some(e: Entity) =>
          val entity = world.createEntity("ITEMUSE"+itemUseId)
            e.getComponent(classOf[Inventory]) match {
              case Some(inventory: Inventory) =>
                val item = inventory.getItemById(itemId) 
                if (item != null) {
                  entity.components += new ItemUse(e, item, e)
                  world.addEntity(entity)
                }
                case _ =>
            }
          case _ => 
        }
        sender ! Success
      case SpawnMessage(userId: String, entityType: String, entityTypeId: Int, x: Int, y: Int) =>
        val id = (new UID()).toString
        entityType.toLowerCase match {
            case "npc" => 
              val entity = EntityFactory.createAI(world, "axis", actorSystem, new Position(x, y))
              //edit the new AIs position
              world.addEntity(entity)

            case "item" => 
              val itemFuture = actorSystem.actorSelection("user/ItemMap") ? GetItem("ITEM"+entityTypeId)
              val item = Await.result(itemFuture, timeout.duration).asInstanceOf[Item]
              if (item != null) {
                val entity = EntityFactory.createLoot(world, item, actorSystem, new Position(x,y))
                world.addEntity(entity)
              } else {
                world.getEntityByTag(s"$userId") match {
                  case Some(e: Entity) =>
                    e.getComponent(classOf[NetworkingActor]) match {
                      case Some(na: NetworkingActor) => 
                        val json = ("type" -> "chat") ~
                          ("message" -> ("Could not create item with id " + entityTypeId)) ~
                          ("sender" -> "system")
                        na.actor ! new ConnectionWrite(compact(render(json)))
                    }
                    
                  case _ => 
                    //no user would be strange
                }
              }

            case _ =>
              // invalid entityType
          }
        sender ! Success
      case _ => println("Error from MessageProcessor.")
    }
  }

  def isEmptySlot(item: Item): Boolean = {
    item.isInstanceOf[EmptySlot]
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => {
      println("Error: from interpreter.")
      sender ! Failure
    }
  }
}
