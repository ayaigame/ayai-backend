package com.ayai.main.networking

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}

class Receptionist(port: Int) extends Thread {
  val server = new ServerSocket(port)

  override def run() = {
    println("I'M RUNNING")
    while (true) {
      val s = server.accept()
      println("GOT TO DA SPOT")

      // val connection: Connection = new SocketConnection(s)
      val connection = new SocketConnection(s)

      val system = ActorSystem("HelloSystem")

      val helloActor = system.actorOf(Props(new RoomService(connection)), name = "helloactor")
      helloActor ! "hello"
      helloActor ! "buenos dias"
    }
  }
}