package com.ayai.main.networking

import java.net.{ServerSocket, Socket}

import akka.actor._
// import akka.routing.RoundRobinRouter
// import akka.util._
// import akka.util.Duration

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

sealed trait NetworkMessage

case class StartConnection() extends NetworkMessage
case class TerminateConnection() extends NetworkMessage
case class ReadFromConnection() extends NetworkMessage
case class WriteToConnection(json: String) extends NetworkMessage

// abstract class Connection extends Actor {
//   def read(): String

//   def write(json: String)

//   // def receive
// }