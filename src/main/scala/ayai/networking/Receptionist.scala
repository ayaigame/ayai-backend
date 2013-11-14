package ayai.networking

import akka.actor.Actor
import akka.actor.ActorRef

import java.net.{ServerSocket, Socket}

class Receptionist(port: Int, manager: ActorRef) extends Thread {
  val server = new ServerSocket(port)

  override def run() = {

    while (true) {
      val s = server.accept()

      println("Connection Accepted!!!" + s.toString)
      
      manager ! CreateConnection(s)
    }
  }
}