package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

import java.net.Socket

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent

sealed trait NetworkAkkaMessage

case class CreateConnection(s: Socket) extends NetworkAkkaMessage
case class StartConnection() extends NetworkAkkaMessage
case class WriteToConnection(connectionId: Int, json: String) extends NetworkAkkaMessage
case class ProcessMessage(message: NetworkMessage) extends NetworkAkkaMessage
case class ResponseMessage extends NetworkAkkaMessage
case class FlushMessages() extends NetworkAkkaMessage
case class QueuedMessages(messages: Array[NetworkMessage]) extends NetworkAkkaMessage
case class AddInterpretedMessage(message: NetworkMessage) extends NetworkAkkaMessage
case class InterpretMessage(message: WebSocketFrameEvent) extends NetworkAkkaMessage
case class SetWebSocketFrame(event: WebSocketFrameEvent)


case class ConnectionGetId() extends NetworkAkkaMessage
case class ConnectionRead() extends NetworkAkkaMessage
case class ConnectionWrite(json: String) extends NetworkAkkaMessage
case class ConnectionIsConnected() extends NetworkAkkaMessage
case class ConnectionKill() extends NetworkAkkaMessage


abstract class Connection(id: Int) extends Actor {
  def getId() = {
    sender ! id
  }

  // def read()

  // def write(json: String)

  // def isConnected()

  // def kill()
}
