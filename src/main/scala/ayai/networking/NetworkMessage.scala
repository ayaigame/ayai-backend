package ayai.networking

abstract class NetworkMessage(connectionId: Int) {
  def getConnectionId: Int = connectionId
}

