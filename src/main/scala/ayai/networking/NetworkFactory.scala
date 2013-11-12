// package ayai.main.networking

// sealed trait NetworkType
// case class SocketConnectionType extends NetworkType
// case class PlayConnectionType extends NetworkType

// class NetworkFactory() {
//   def makeConnection(connectionType: NetworkType = SocketConnectionType()): Connection = connectionType match {
//     case SocketConnectionType() =>
//       return (new SocketConnection())
//     case PlayConnectionType() =>
//       return (new PlayConnection())
//   }

// }