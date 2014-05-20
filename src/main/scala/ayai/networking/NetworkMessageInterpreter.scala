package ayai.networking

/** Ayai Imports **/
import ayai.gamestate._
import ayai.actions._
import ayai.persistence._
import ayai.apps.Constants
import ayai.networking.chat._

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

/** Socko Imports **/
import org.mashupbots.socko.routes._
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.util.Random
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.collection.immutable.StringOps

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.rmi.server.UID

class NetworkMessageInterpreter extends Actor {
  implicit val timeout = Timeout(2 seconds)
  val queue = context.system.actorSelection("user/MQueue")
  val socketUserMap = context.system.actorSelection("user/SocketUserMap")
  val userRoomMap = context.system.actorSelection("user/UserRoomMap")
  // TODO: We need a room lookup that is better than this
  val roomList = context.system.actorSelection("user/RoomList")

  implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Remove quotes from beginning and end of string if they exist
   */
  def stripQuotes(s: StringOps): String = {
    (s.head, s.last) match {
      case('\"', '\"') => s.tail.take(s.length - 2)
      case _ => s
    }
  }

  def lookUpUserBySocketId(socketId: String): String = {
    val future = socketUserMap ? GetUserId(socketId)
    Await.result(future, timeout.duration).asInstanceOf[String]
  }

  def lookUpWorldByUserId(userId: String): Int = {
    val future = userRoomMap ? GetWorld(userId)
    Await.result(future, timeout.duration).asInstanceOf[RoomWorld].id
  }

  def lookUpWorldById(id: Int): RoomWorld = {
    val future = roomList ? GetWorldById(id)
    Await.result(future, timeout.duration).asInstanceOf[Option[RoomWorld]] match {
      case Some(room: RoomWorld) => room
    }
  }

  def interpretMessage(wsFrame: WebSocketFrameEvent) = {
    val rootJSON = parse(wsFrame.readText)
    val msgType: String = stripQuotes(compact(render(rootJSON \ "type")))
    if(msgType == "interact") {
      println(wsFrame.readText)
    }
    val userId = msgType match {
      case "init" => ""
      case _ =>
        lookUpUserBySocketId(wsFrame.webSocketId)
    }
    var worldId = msgType match {
      case "init" => 0
      case _ =>
        lookUpWorldByUserId(userId)
    }

    msgType match {
      case "init" =>
        val id = (new UID()).toString

        val characterName: String = stripQuotes(compact(render(rootJSON \ "name")))

        worldId = CharacterTable.getCharacter(characterName) match {
          case Some(characterRow: CharacterRow) =>
            characterRow.room_id.toInt
          case _ => 0
        }

        context.system.actorOf(Props(new SockoSender(wsFrame)), "SockoSender" + id)
        context.system.actorSelection("user/SocketUserMap") ! AddSocketUser(wsFrame.webSocketId, id)
        context.system.actorSelection("user/UserRoomMap") ! AddAssociation(id, lookUpWorldById(worldId))

        queue ! new AddInterpretedMessage(worldId, new AddNewCharacter(id, characterName, Constants.STARTING_X, Constants.STARTING_Y))

      case "move" =>
        //TODO: Add exceptions and maybe parse shit a bit more intelligently
        val start: Boolean = compact(render(rootJSON \ "start")).toBoolean
        var direction: MoveDirection = new MoveDirection(0,0)
        if(start) {
          val dir: Int = compact(render(rootJSON \ "dir")).toInt
          direction  = dir match {
            case 0 => UpDirection
            case 1 => UpRightDirection
            case 2 => RightDirection
            case 3 => DownRightDirection
            case 4 => DownDirection
            case 5 => DownLeftDirection
            case 6 => LeftDirection
            case 7 => UpLeftDirection
            case _ => {
              println("Direction not found, in Interpreter")
              new MoveDirection(0,0)
            }
          }
        }
        queue ! new AddInterpretedMessage(worldId, new MoveMessage(userId, start, direction))

      case "attack" =>
        queue ! new AddInterpretedMessage(worldId, new AttackMessage(userId))

      case "chat" =>
        val message: String = stripQuotes(compact(render(rootJSON \ "message")))
        val sender: String = stripQuotes(compact(render(rootJSON \ "sender")))
        val account = AccountTable.getAccount(sender)
        account match {
          case Some(a: Account) =>
            context.system.actorOf(Props[ChatReceiver]) ! new ChatHolder(new PublicChat(message, a), lookUpWorldById(worldId))
          case _ =>
            println(s"Could not find user $sender")
        }

      case "open" =>
        val containerId: String = (rootJSON \ "containerId").extract[String]
        queue ! new AddInterpretedMessage(worldId, new OpenMessage(userId, containerId))

      case "chars" =>
        val accountName: String = (rootJSON \ "accountName").extract[String]
        queue ! new CharacterList(userId, accountName)
      case "equip" =>
        val slot: Int = (rootJSON \ "slot").extract[Int]
        val equipmentType: String = (rootJSON \ "equipmentType").extract[String]
        queue ! new AddInterpretedMessage(worldId, new EquipMessage(userId, slot, equipmentType))
      case "unequip" =>
        val equipmentType = (rootJSON \ "equipmentType").extract[String]
        queue ! new AddInterpretedMessage(worldId, new UnequipMessage(userId, equipmentType))
      case "dropitem" =>
        val slot = (rootJSON \ "slot").extract[Int]
        queue ! new AddInterpretedMessage(worldId, new DropItemMessage(userId, slot))
      case "quest-accept" =>
        println(wsFrame.readText)
        val entityId = (rootJSON \ "entityId").extract[String]
        val questId = (rootJSON \ "questId").extract[Int]
        queue ! new AddInterpretedMessage(worldId, new AcceptQuestMessage(userId, entityId, questId))
      case "quest-abandon" =>
        val questId = (rootJSON \ "questId").extract[Int]
        queue ! new AddInterpretedMessage(worldId, new AbandonQuestMessage(userId, questId))
      case "loot-pickup" =>
        val entityId = (rootJSON \ "entityId").extract[String]
        val inventoryIds = (rootJSON \ "itemId").extract[Int]
        queue ! new AddInterpretedMessage(worldId, new LootMessage(userId, entityId, inventoryIds))
      case "interact" =>
        val entityId = (rootJSON \ "entityId").extract[String]
        queue ! new AddInterpretedMessage(worldId, new InteractMessage(userId, entityId))
      case "createai" =>
        queue ! new AddInterpretedMessage(worldId, CreateAIMessage)
      case "useitem" =>
        // println("use item received")
        val itemId = (rootJSON \ "itemId").extract[Int]
        queue ! new AddInterpretedMessage(worldId, new UseItemMessage(userId, itemId))
      case "spawn" =>
        println("spawned")
        val entityType = (rootJSON \ "entityType").extract[String]
        val entityTypeId = (rootJSON \ "entityTypeId").extract[Int]
        val x = (rootJSON \ "x").extract[Int]
        val y = (rootJSON \ "y").extract[Int]
        queue ! new AddInterpretedMessage(worldId, new SpawnMessage(userId, entityType, entityTypeId, x, y))
      case _ =>
        println("Unknown message in NetworkMessageInterpreter: " + msgType)
    }
  }

  def receive = {
    case InterpretMessage(message) => interpretMessage(message)
    case _ => println("Error: from interpreter.")
  }
}
