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

case class ProcessMessage(message: NetworkMessage)
case object FlushMessages
case class QueuedMessages(messages: Array[NetworkMessage])
case class AddInterpretedMessage(message: NetworkMessage)
case class InterpretMessage(message: WebSocketFrameEvent)
case class ConnectionWrite(json: String)

sealed trait NetworkMessage

case class JSONMessage(message: String) extends NetworkMessage
case class AddNewCharacter(socketId: String, id: String, characterName: String, x: Int, y: Int) extends NetworkMessage
case class RemoveCharacter(id: String) extends NetworkMessage
case class MoveMessage(socketId: String, start: Boolean, direction: MoveDirection) extends NetworkMessage
case class ItemMessage(id : String, itemAction : ItemAction) extends NetworkMessage
case class AttackMessage(socketId: String) extends NetworkMessage
case class SocketCharacterMap(socketId: String, id: String) extends NetworkMessage
case class PublicChatMessage(message: String, sender: String) extends NetworkMessage

case class LoginPost(request: HttpRequestEvent) extends NetworkMessage
case class RegisterPost(request: HttpRequestEvent) extends NetworkMessage
case class RecoveryPost(request: HttpRequestEvent) extends NetworkMessage
case class OpenMessage(socketId: String, containerId: String) extends NetworkMessage
case class CharacterList(socketId: String, accountName: String) extends NetworkMessage
