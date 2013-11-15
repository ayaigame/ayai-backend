package ayai.networking

/** Akka Imports **/
import akka.actor.{Actor, ActorRef}

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class ConnectionReader(connection: ActorRef, interpreter: ActorRef) extends Service(connection) {
  implicit val timeout = Timeout(5 seconds)
  val id: Int = {
    var future = connection ? ConnectionGetId()
    Await.result(future, timeout.duration).asInstanceOf[Int]
  }

  def acceptMessages = {
    var future = connection ? ConnectionIsConnected()
    var isConnected = Await.result(future, timeout.duration).asInstanceOf[Boolean]
    while(isConnected) {
      future = connection ? ConnectionRead()
      var request = Await.result(future, timeout.duration).asInstanceOf[String]
      if(request.length > 0) {
        interpreter ! new InterpretMessage(id, request)
      }
      future = connection ? ConnectionIsConnected()
      isConnected = Await.result(future, timeout.duration).asInstanceOf[Boolean]
    }
    connection ! ConnectionKill()
  }

  def receive = {
    case StartConnection() => acceptMessages
    case _ => println("Error: incomprehensible message.")
  }
}
