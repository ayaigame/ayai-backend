// package com.ayai.main.networking

// import akka.actor._
// import akka.routing.RoundRobinRouter
// import akka.util.Duration
// import akka.util.duration._

// sealed trait NetworkMessage

// case class CreateConnection(s: Socket) extends NetworkMessage
// case class TerminateConnection() extends NetworkMessage
// case class ReadFromConnection() extends NetworkMessage
// case class WriteToConnection() extends NetworkMessage

// class ConnectionActor extends Actor {

//   def createConnection(port: Int, connectionType: NetworkType = new SocketConnectionType) = {
//       println("I'm an actor!")
//   }

//   def receive = {
//     case CreateConnection(s) =>
//       sender ! createConnection(port)
//   }
// }