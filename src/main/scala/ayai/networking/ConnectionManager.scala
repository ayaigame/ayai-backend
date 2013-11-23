package ayai.networking

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import java.rmi.server.UID
import java.net.{ServerSocket, Socket}
import scala.collection.immutable.HashMap

class ConnectionManager(networkSystem: ActorSystem, interpreter: ActorRef) extends Actor {
  var connections: Map[Int, Connection] = new HashMap[Int, Connection]()
  var nextId: Int = 0

  def createSocketConenction(s: Socket) = {
    val connection: Connection = new SocketConnection(nextId, s)
    connections = connections + (nextId -> connection)
    nextId = nextId + 1
    val connectionReader: ActorRef = networkSystem.actorOf(Props(new ConnectionReader(connection, interpreter)), name = (new UID()).toString)
    connectionReader ! StartConnection()
  }

  def writeToConnection(connectionId: Int, json: String) = {
    val connection = connections(connectionId)
    connection.write(json)
  }

  def receive = {
    case CreateConnection(s) => createSocketConenction(s)
    case WriteToConnection(connectionId, json) => writeToConnection(connectionId, json)
    case _ => println("Error: from connection manager.")
  }
}

