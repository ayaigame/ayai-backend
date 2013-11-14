package ayai.networking

import java.net.{ServerSocket, Socket}

import akka.actor._
// import akka.routing.RoundRobinRouter
// import akka.util._
// import akka.util.Duration

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

sealed trait NetworkAkkaMessage

case class CreateConnection(s: Socket) extends NetworkAkkaMessage
case class StartConnection() extends NetworkAkkaMessage
case class TerminateConnection() extends NetworkAkkaMessage
case class ReadFromConnection() extends NetworkAkkaMessage
case class WriteToConnection(json: String) extends NetworkAkkaMessage
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
