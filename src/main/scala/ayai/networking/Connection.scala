package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

import ayai.actions._

import java.net.Socket

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent


case class ProcessMessage(message: NetworkMessage)
case class FlushMessages()
case class QueuedMessages(messages: Array[NetworkMessage])
case class AddInterpretedMessage(message: NetworkMessage)
case class InterpretMessage(message: WebSocketFrameEvent)
case class ConnectionWrite(json: String)

sealed trait NetworkMessage

case class JSONMessage(message: String) extends NetworkMessage
//case class AddNewPlayer(id: String) extends NetworkMessage
case class AddNewPlayer(id: String, x: Int, y: Int) extends NetworkMessage
case class RemovePlayer(id: String) extends NetworkMessage
case class MoveMessage(webSocket: WebSocketFrameEvent, start: Boolean, direction: MoveDirection) extends NetworkMessage
case class ItemMessage(id : String, itemAction : ItemAction) extends NetworkMessage
case class SocketPlayerMap(webSocket: WebSocketFrameEvent, id: String) extends NetworkMessage
case class PublicChatMessage(message: String, sender: String) extends NetworkMessage
