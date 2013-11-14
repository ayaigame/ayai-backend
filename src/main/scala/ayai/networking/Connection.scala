package ayai.networking

import java.net.Socket

sealed trait NetworkAkkaMessage

case class CreateConnection(s: Socket) extends NetworkAkkaMessage
case class StartConnection() extends NetworkAkkaMessage
case class TerminateConnection() extends NetworkAkkaMessage
case class ReadFromConnection() extends NetworkAkkaMessage
case class WriteToConnection(connectionId: Int, json: String) extends NetworkAkkaMessage
case class ProcessMessage(message: NetworkMessage) extends NetworkAkkaMessage
case class ResponseMessage extends NetworkAkkaMessage
case class FlushMessages() extends NetworkAkkaMessage
case class QueuedMessages(messages: Array[NetworkMessage]) extends NetworkAkkaMessage
case class AddInterpretedMessage(message: NetworkMessage) extends NetworkAkkaMessage
case class InterpretMessage(connectionId: Int, message: String) extends NetworkAkkaMessage

abstract class Connection(id: Int) {
  def getId(): Int = {
    id
  }

  def read(): String

  def write(json: String)

  def isConnected(): Boolean

  def kill()
}
