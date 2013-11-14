package ayai.networking

/** Akka Imports **/
import akka.actor.{Actor, ActorRef}

class ConnectionReader(connection: Connection, interpreter: ActorRef) extends Service(connection) {
  def acceptMessages = {
    while(connection.isConnected()) {
      var request = connection.read()
      if(request.length > 0) {
        interpreter ! new InterpretMessage(connection.getId, request)
      }
    }
    connection.kill()
  }

  def receive = {
    case StartConnection() => acceptMessages
    case _ => println("Error: incomprehensible message.")
  }
}
