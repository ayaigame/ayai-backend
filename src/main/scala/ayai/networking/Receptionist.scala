package ayai.networking

import java.rmi.server.UID
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props

import java.net.{ServerSocket, Socket}

class Receptionist(port: Int, networkSystem: ActorSystem, queue: ActorRef) extends Thread {
  val server = new ServerSocket(port)

  override def run() = {

    while (true) {
      val s = server.accept()

      val connection: Connection= new SocketConnection(s)

      val roomService = networkSystem.actorOf(Props(new RoomService(connection, queue)), name = (new UID()).toString)
      roomService ! StartConnection()
    }
  }
}