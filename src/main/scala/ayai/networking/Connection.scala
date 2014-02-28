package ayai.networking

/** Ayai Imports **/
import ayai.actions._

/** Akka Imports **/
import akka.actor.Actor

/** Socko Imports **/
import org.mashupbots.socko.events.HttpRequestEvent
import org.mashupbots.socko.events.WebSocketFrameEvent

/** External Imports **/
import java.net.Socket

sealed trait Message
sealed trait ProcessorMessage extends Message
sealed trait EventMessage extends Message
sealed trait NetworkMessage extends Message

case class ProcessMessage(message: Message)
case class FlushMessages(world: String)
case class QueuedMessages(messages: Array[Message])
case class AddInterpretedMessage(world: String, message: Message)
case class InterpretMessage(message: WebSocketFrameEvent)
case class ConnectionWrite(json: String)

case class JSONMessage(message: String) extends ProcessorMessage
case class AddNewCharacter(userId: String, characterName: String, x: Int, y: Int) extends ProcessorMessage
case class RemoveCharacter(id: String) extends ProcessorMessage
case class MoveMessage(socketId: String, start: Boolean, direction: MoveDirection) extends ProcessorMessage
case class ItemMessage(id : String, itemAction : ItemAction) extends ProcessorMessage
case class AttackMessage(socketId: String) extends ProcessorMessage
case class SocketCharacterMap(socketId: String, id: String) extends ProcessorMessage

case class PublicChatMessage(message: String, sender: String) extends NetworkMessage
case class LoginPost(request: HttpRequestEvent) extends NetworkMessage
case class RegisterPost(request: HttpRequestEvent) extends NetworkMessage
case class RecoveryPost(request: HttpRequestEvent) extends NetworkMessage
case class OpenMessage(socketId: String, containerId: String) extends NetworkMessage
case class CharacterList(socketId: String, accountName: String) extends NetworkMessage
