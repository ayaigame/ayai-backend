// package ayai.networking
// /**
//  * ayai.networking.messaging.MessagingSender
//  * Actor that sends messages to a player over a websocket
//  */

// /** Ayai Imports **/
// import ayai.persistence.User

// /** Akka Imports **/
// import akka.actor.{Actor, ActorRef}

// /** Socko Imports **/
// import org.mashupbots.socko.events.WebSocketFrameEvent
// import org.mashupbots.socko.events.HttpResponseStatus
// import org.mashupbots.socko.routes._
// import org.mashupbots.socko.infrastructure.Logger
// import org.mashupbots.socko.webserver.WebServer
// import org.mashupbots.socko.webserver.WebServerConfig

// class SockoConnection(interpreter: ActorRef) extends Actor {
//   def receive = {
//     case wsFrame: WebSocketFrame => interpreter ! wsFrame

//     case ConnectionWrite(message: String) => write(message)

//     case _ => println("Unknown message type")
//    }
// }
