package com.ayai.main.networking

sealed trait NetworkType
case class SocketConnectionType extends NetworkType
case class PlayConnectionType extends NetworkType

class NetworkFactory {
  def makeConnection(ip: String, port: Int, connectionType: NetworkType = SocketConnectionType()): Connection = connectionType match {
    case SocketConnectionType() =>
      return (new SocketConnection(ip, port))
    case PlayConnectionType() =>
      return (new PlayConnection(ip, port))
  }

}