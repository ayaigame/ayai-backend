package ayai.networking

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import java.rmi.server.UID
import java.net.{ServerSocket, Socket}
import scala.collection.immutable.HashMap

class ConnectionManager(networkSystem: ActorSystem, interpreter: ActorRef) extends Actor {
  var connections: Map[Int, ActorRef] = new HashMap[Int, ActorRef]()
  var nextId: Int = 0

  def createSocketConenction(s: Socket) = {
    val connection: ActorRef = networkSystem.actorOf(Props(new SocketConnection(nextId, s)), name = (new UID()).toString)
    connections = connections + (nextId -> connection)

    implicit val timeout = Timeout(5 seconds)
    var future = connection ? ConnectionGetId()
    println(Await.result(future, timeout.duration).asInstanceOf[Int])

    nextId = nextId + 1
    val connectionReader: ActorRef = networkSystem.actorOf(Props(new ConnectionReader(connection, interpreter)), name = (new UID()).toString)
    connectionReader ! StartConnection()
  }

  def writeToConnection(connectionId: Int, json: String) = {
    val connection = connections(connectionId)
    connection ! ConnectionWrite(json)
  }

  def receive = {
    case CreateConnection(s) => createSocketConenction(s)
    case WriteToConnection(connectionId, json) => writeToConnection(connectionId, json)
    case _ => println("Error: from connection manager.")
  }
}

