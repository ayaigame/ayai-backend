package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

import java.net.Socket

/** Socko Imports **/
import org.mashupbots.socko.events.WebSocketFrameEvent


case class ProcessMessage(message: NetworkMessage)
case class FlushMessages()
case class QueuedMessages(messages: Array[NetworkMessage])
case class AddInterpretedMessage(message: NetworkMessage)
case class InterpretMessage(message: WebSocketFrameEvent)
case class ConnectionWrite(json: String)

case class NetworkMessage(message: String)
