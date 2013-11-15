package ayai.networking

import java.net.{ServerSocket, Socket}
import java.io._
import scala.io._
import scala.util.parsing.json._

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props


class SocketConnection(id: Int, s: Socket) extends Connection(id) {
  val in = new BufferedSource(s.getInputStream())
  val out = new PrintStream(s.getOutputStream())

  // override def receive = {
  def receive = {
    case ConnectionGetId() => getId()
    case ConnectionRead() => read()
    case ConnectionWrite(json: String) => write(json)
    case ConnectionIsConnected() => isConnected()
    case ConnectionKill() => kill()
  }

  def isConnected() = {
    sender ! s.isConnected()
  }

  def kill() = {
    s.close()
  }

  def read() = {
    //If multiple lines, the rest will be lost...
    //FIX THIS
    if (s.isConnected()) {
      var lines = in.getLines()
      if(lines.hasNext) {
        sender ! lines.next()
      }
    }   
    sender ! ""
  }

  def write(json: String) = {
    if (s.isConnected()) {
      out.println(json)
      out.flush()
    }
  }
}
