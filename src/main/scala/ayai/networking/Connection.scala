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
case class FlushMessages()
case class QueuedMessages(messages: Array[NetworkMessage])
case class AddInterpretedMessage(message: NetworkMessage)
case class InterpretMessage(message: WebSocketFrameEvent)
case class ConnectionWrite(json: String)

sealed trait NetworkMessage

case class JSONMessage(message: String) extends NetworkMessage
case class AddNewCharacter(webSocket: WebSocketFrameEvent, id: String, characterName: String, x: Int, y: Int) extends NetworkMessage
case class RemoveCharacter(id: String) extends NetworkMessage
case class MoveMessage(webSocket: WebSocketFrameEvent, start: Boolean, direction: MoveDirection) extends NetworkMessage
case class ItemMessage(id : String, itemAction : ItemAction) extends NetworkMessage
case class AttackMessage(webSocket: WebSocketFrameEvent) extends NetworkMessage
case class SocketCharacterMap(webSocket: WebSocketFrameEvent, id: String) extends NetworkMessage
case class PublicChatMessage(message: String, sender: String) extends NetworkMessage
case class EquipMessage(webSocket: WebSocketFrameEvent, slot: String, equipmentType: String) extends NetworkMessage

case class LoginPost(request: HttpRequestEvent) extends NetworkMessage
case class RegisterPost(request: HttpRequestEvent) extends NetworkMessage
case class RecoveryPost(request: HttpRequestEvent) extends NetworkMessage
case class OpenMessage(webSocket: WebSocketFrameEvent, containerId : String) extends NetworkMessage
case class CharacterList(webSocket: WebSocketFrameEvent, accountName: String) extends NetworkMessage
