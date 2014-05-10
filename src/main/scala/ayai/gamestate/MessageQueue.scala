package ayai.gamestate

import ayai.gamestate._
import ayai.networking._

/** Akka Imports **/
import akka.actor.Actor

/** External Imports **/
import scala.collection.mutable.{ArrayBuffer, HashMap, Map=>MMap}

class MessageQueue extends Actor{
  var messages: MMap[Int, ArrayBuffer[Message]] = new HashMap[Int,
  ArrayBuffer[Message]]().withDefaultValue(new ArrayBuffer[Message]())

  /**
  * Returns all the messages in the queue to the sender and clears out the queue.
  **/
  def flushMessages(worldId: Int) = {
    var currentMessages: Array[Message] = messages(worldId).toArray
    messages(worldId) = new ArrayBuffer[Message]()
    sender ! new QueuedMessages(currentMessages)
  }

  def addInterpretedMessage(worldId: Int, message: Message) = {
    messages(worldId) += message
  }

  def receive = {
    case FlushMessages(worldId: Int) => flushMessages(worldId)
    case AddInterpretedMessage(worldId: Int, message: Message) => addInterpretedMessage(worldId, message)
    case _ => println("Error: from queue.")
  }
}
