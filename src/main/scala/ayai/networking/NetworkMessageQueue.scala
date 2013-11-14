package ayai.networking

/** Ayai Imports **/
// import ayai.networking.Connection

/** Akka Imports **/
import akka.actor.Actor
import akka.actor.ActorRef

import scala.collection.mutable.ArrayBuffer

class NetworkMessageQueue extends Actor{
  var messages : ArrayBuffer[NetworkMessage] = new ArrayBuffer[NetworkMessage]()

  def flushMessages = {
    var currentMessages: Array[NetworkMessage] = messages.toArray
    messages = new ArrayBuffer[NetworkMessage]()
    sender ! new QueuedMessages(currentMessages)
  }

  def addInterpretedMessage(message: NetworkMessage) = {
    messages += message
  }

  def receive = {
    case FlushMessages() => flushMessages
    case AddInterpretedMessage(message) => addInterpretedMessage(message)
    case _ => println("Error: from queue.")
  }
}
