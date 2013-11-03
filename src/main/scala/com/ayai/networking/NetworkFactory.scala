package com.ayai.networking

sealed trait NetworkType
case class SocketConnectionType extends NetworkType
case class PlayConnectionType extends NetworkType

class NetworkFactory {
  def buildConnection(ip: String, port: Int, connectionType: NetworkType): Connection = connectionType match {
    case SocketConnectionType() =>
      return (new SocketConnection(ip, port))
    case PlayConnectionType() =>
      return (new PlayConnection(ip, port))
  }

}