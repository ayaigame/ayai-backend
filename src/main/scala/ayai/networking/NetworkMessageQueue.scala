package ayai.networking

/** Ayai Imports **/
// import ayai.networking.Connection

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef

import scala.collection.mutable.ArrayBuffer

class NetworkMessageQueue(interpreter: ActorRef) extends Actor{
  var messages : ArrayBuffer[NetworkMessage] = new ArrayBuffer[NetworkMessage]()

  def flushMessages = {
    var currentMessages: Array[NetworkMessage] = messages.toArray
    messages = new ArrayBuffer[NetworkMessage]()
    sender ! new QueuedMessages(currentMessages)
  }

  def addRawMessage(message: String) = {
    interpreter ! new InterpretMessage(message)
  }

  def addInterpretedMessage(message: NetworkMessage) = {
    messages:+ message
  }

  def receive = {
    case FlushMessages() => flushMessages
    case AddRawMessage(message: String) => addRawMessage(message)
    case AddInterpretedMessage(message) => addInterpretedMessage(message)
    case _ => println("Error: from queue.")
  }
}
