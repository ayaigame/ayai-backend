package com.ayai.main.networking

class Receptionist extends Thread {
  val server = new ServerSocket(port)

  while (true) {
      val s = server.accept()

      val connection: Connection = new SocketConnection(s)
    }
}