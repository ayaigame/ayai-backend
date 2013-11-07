package com.ayai.main.networking

import java.rmi.server.UID
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}

class Receptionist(port: Int) extends Thread {
  val server = new ServerSocket(port)

  override def run() = {
    val system = ActorSystem("HelloSystem")

    while (true) {
      val s = server.accept()

      val connection: Connection= new SocketConnection(s)


      val helloActor = system.actorOf(Props(new RoomService(connection)), name = (new UID()).toString)
      helloActor ! StartConnection()
    }
  }
}