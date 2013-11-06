package com.ayai.main.networking

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._
import scala.util.parsing.json._

class RoomService(connectionRef: ActorRef) extends Actor {
// class RoomService(connection: SocketConnection) extends Actor {
  // def doStuff = {
  //   while(true)
  //     var request = connectionRef ! ReadFromConnection()
  //     val result = JSON.parseFull(request)
  //     val player_id = result match {
  //       case Some(e: Map[String, Int]) => e("player_id")
  //       case _ => "Failed player request."
  //     }
  //     println(player_id)
  //   }
  // }

  def receive = {
    case StartConnection() => connectionRef ! ReadFromConnection()
    case _       => println("huh?")
  }
}
