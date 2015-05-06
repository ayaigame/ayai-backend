package ayai.gamestate

import akka.actor.Actor

case class AddSocketUser(socketId: String, userId: String)
case class RemoveSocketUser(socketId: String)
case class GetUserId(socketId: String)

class SocketUserMap extends Actor {
  val socketUserMap = collection.mutable.HashMap[String, String]()

  def receive = {
    case AddSocketUser(socketId, userId) => socketUserMap(socketId) = userId
    case RemoveSocketUser(socketId) => socketUserMap -= socketId
    case GetUserId(socketId) => sender ! socketUserMap(socketId)
  }
}
