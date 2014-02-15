package ayai.networking

/** Ayai Imports **/
import ayai.gamestate.{Effect, EffectType}
import ayai.actions._
import ayai.components._
import ayai.networking.chat._
import ayai.persistence.CharacterTable
import ayai.factories.EntityFactory
import ayai.apps.{Constants, GameLoop}

import crane.{Entity, World}

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef, Props}

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import scala.util.Random
import scala.collection.concurrent.{Map => ConcurrentMap}
import scala.collection.mutable.ArrayBuffer

import java.rmi.server.UID

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

import org.slf4j.{Logger, LoggerFactory}


class NetworkMessageProcessor(actorSystem: ActorSystem, world: World, socketMap: ConcurrentMap[String, String]) extends Actor {
  implicit val formats = Serialization.formats(NoTypeHints)
  val characterTable = actorSystem.actorOf(Props(new CharacterTable()))
  private val log = LoggerFactory.getLogger(getClass)

  def processMessage(message: NetworkMessage) {
    message match {
      //Should take characterId: Long as a parameter instead of characterName
      //However can't do that until front end actually gives me the characterId
      case AddNewCharacter(webSocket: WebSocketFrameEvent, id: String, characterName: String, x: Int, y: Int) => {
        val actor = actorSystem.actorSelection("user/SockoSender"+id)
        EntityFactory.loadCharacter(world, webSocket, id, characterName, x, y, actor) //Should use characterId instead of characterName
      }

      case RemoveCharacter(id: String) => {
        println("Removing character: " + id)
        (world.getEntityByTag("CHARACTER" + socketMap(id))) match {
          case None =>
            System.out.println(s"Can't find character attached to socket $id.")
          case Some(character : Entity) =>
            character.kill
            socketMap.remove(id)
        }
        println("CHARACTER KILLED")
      }

      case MoveMessage(webSocket: WebSocketFrameEvent, start: Boolean, direction: MoveDirection) => {
        //println("Direction: " + direction.xDirection.toString + ", " + direction.yDirection.toString)
        val id: String = socketMap(webSocket.webSocketId)
        (world.getEntityByTag("CHARACTER"+id)) match {
          case None =>
            println("Can't find character attached to id: " + id)
          case Some(e : Entity) =>
              val oldMovement = (e.getComponent(classOf[Actionable])) match {
                case Some(oldMove : Actionable) =>
                  oldMove.active = start
                  oldMove.action = direction
                case _ =>
                  log.warn("a07270d: getComponent failed to return anything")

              }
        }
      }

       // give id of the item, and what action it should do (equip, use, unequip, remove from inventory)
      case AttackMessage(webSocket : WebSocketFrameEvent) => {
        //create a projectile
        println("Created Bullet")
        val id : String = socketMap(webSocket.webSocketId)
        val bulletId = (new UID()).toString

        (world.getEntityByTag("CHARACTER"+id)) match {

        //for rob temporary
        //initiator.getComponent(classOf[Health]).currentHealth -= 10
        //initiator.getComponent(classOf[Mana]).currentMana -= 20
        case Some(initiator : Entity) =>
          val position = initiator.getComponent(classOf[Position])
          val movable = initiator.getComponent(classOf[Actionable])
          val character = initiator.getComponent(classOf[Character])
          (position, movable, character) match {
            case(Some(pos: Position), Some(a : Actionable), Some(c : Character)) =>
              val m = a.action match {
                case (move : MoveDirection) => move
                case _ => println("Not match for movedirection")
                return

              }
              val upperLeftx = pos.x
              val upperLefty = pos.y

              println("Player position: " + upperLeftx.toString + ", " + upperLefty.toString)


              val xDirection = m.xDirection
              val yDirection = m.yDirection

              val topLeftOfAttackx = (33 * xDirection) + upperLeftx
              val topLeftOfAttacky = (33 * yDirection) + upperLefty

              println("Attack box position: " + topLeftOfAttackx.toString + ", " + topLeftOfAttacky.toString)

              val p: Entity = world.createEntity("ATTACK"+bulletId)

              p.components += (new Position(topLeftOfAttackx, topLeftOfAttacky))
              p.components += (new Bounds(10, 10))
              p.components += (new Attack(12));
              p.components += (new Frame(10,0))
              //p.components += (c)
              world.addEntity(p)
              world.groups("ROOM"+Constants.STARTING_ROOM_ID) += p
            case _ =>
              log.warn("424e244: getComponent failed to return anything")

          }
          case _ =>
            log.warn("8a87265: getComponent failed to return anything")
        } 
      }

      case ItemMessage(id : String, itemAction : ItemAction) => {

      }

      case OpenMessage(webSocket: WebSocketFrameEvent, containerId : String) => {
          println("Open Received!")
          val inventory = new ArrayBuffer[Item]()
        inventory += new Weapon(name = "Orcrist", value = 100000000,
  weight = 10, range = 1, damage = 500000, damageType = "physical")

          val fakeChest = new Inventory(inventory)

          val jsonLift = 
            ("type" -> "open") ~
            ("containerId" -> containerId) ~
            (fakeChest.asJson)


          println(compact(render(jsonLift)))

          webSocket.writeText(compact(render(jsonLift)))
      }

      case SocketCharacterMap(webSocket: WebSocketFrameEvent, id: String) => {
        socketMap(webSocket.webSocketId) = id
      }

      case CharacterList(webSocket: WebSocketFrameEvent, accountName: String) => {
        characterTable ! new CharacterList(webSocket, accountName)
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
      case _ => println("Error from NetworkMessageProcessor.")
    } 
  }

  def receive = {
    case ProcessMessage(message) => processMessage(message)
    case _ => println("Error: from interpreter.")
  }
}
