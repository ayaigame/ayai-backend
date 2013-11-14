package ayai.networking

/** Akka Imports **/
import akka.actor.Actor

abstract class NetworkMessage(connectionId: Int) {
  def getConnectionId: Int = connectionId
}

