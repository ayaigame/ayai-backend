package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

class NetworkMessageQueue extends Actor{
  var messages : ArrayBuffer[NetworkMessage] = new ArrayBuffer[NetworkMessage]()

  /**
  * Returns all the messages in the queue to the sender and clears out the queue.
  **/
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
