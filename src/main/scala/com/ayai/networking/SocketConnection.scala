package com.ayai.main.networking

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._
import scala.util.parsing.json._


// use:
// echo "{\"player_id\": 1}" | nc 144.118.167.159 8007
// to test
class SocketConnection(ip: String, port: Int) extends Connection(ip: String, port: Int) {
  def run() : Unit = {
    val server = new ServerSocket(port)

    while (true) {
      val s = server.accept()
      val in = new BufferedSource(s.getInputStream()).getLines()
      val out = new PrintStream(s.getOutputStream())

      var request = in.next()
      val result = JSON.parseFull(request)
      val player_id = result match {
        case Some(e: Map[String, Int]) => e("player_id")
        case _ => println("Failed player request.")
      }
      println(player_id)
      // out.println(in.next())
      // out.flush()
      s.close()
    }
  }
}
