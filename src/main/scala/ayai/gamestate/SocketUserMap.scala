package ayai.gamestate

import crane.Entity

case class AddSocketUser(socketId: String, userId: String)
case class RemoveSocketUser(socketId: String)
case class GetUserId(socketId: String)

class SocketUserMap extends Actor {
  val socketUserMap: HashMap[String, String] = HashMap[String, String]()

  def receive = {
    case AddSocketUser(socketId: String, userId: String) => socketUserMap(socketId) = userId
    case RemoveSocketUser(socketId: String) => socketUserMap -= socketId
    case GetUserId(socketId: String) => sender ! socketUserMap(socketId)
  }

}
