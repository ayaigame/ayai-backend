package com.ayai.networking

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._

sealed trait NetworkMessage

case class CreateConnection(ip: String, port: Int) extends NetworkMessage
case class TerminateConnection(ip: String, port: Int) extends NetworkMessage

class ConnectionActor extends Actor {

  def createConnection(ip: String, port: Int, connectionType: NetworkType = new SocketConnectionType) = {
      println("I'm an actor!")
  }

  def receive = {
    case CreateConnection(ip, port) =>
      sender ! createConnection(ip, port)
  }
}