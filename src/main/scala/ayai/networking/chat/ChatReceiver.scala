// package ayai.networking.chat
// /**
//  * ayai.networking.chat.ChatReceiver
//  * Actor that receives chats sent by the player over sockets and stores them
//  * and forwards them to the actor that forwards chats back to players
//  */

// /** Ayai Imports **/
// import ayai.networking.ConnectionWrite
// //import ayai.persistence.{StoredChat, StoredChats, NewStoredChat}

// /** Akka Imports **/
// import akka.actor.Actor

// /** External Imports **/
// //import scala.slick.driver.H2Driver.simple.{Database,Session}
// import scala.concurrent.duration._
// import scala.concurrent.Await


// class ChatReceiver extends Actor {
//   var typeOfChat: String = ""

//   def receive = {
//     case ChatHolder(chat) =>
//       val received = reroute(chat)
//       store(chat, received)
//       context.stop(self)

//     case _ => println("Unknown chat type")
//    }

//   private def store(chat: Chat, received: Boolean) = {
//     //var storedChat = None : Option[NewStoredChat]
//     //// Create the type of stored chat based on the type of chat we receive
//     //// This is since Public Chats do not have a reciever and are automatically
//     //// considered "received"
//     //chat match {
//     //  case PrivateChat(text, sender, receiver) =>
//     //    storedChat = Some(NewStoredChat(text, sender.id, receiver.id, received))
//     //  case PublicChat(text, sender) =>
//     //    storedChat = Some(NewStoredChat(text, sender.id, -1, true))
//     //}

//     //// Insert the chat into the DB
//     //storedChat match {
//     //  case None => 
//     //    println("Should not get here - this is a private method")
//     //  case Some(chat) =>
//     //    Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
//     //      StoredChats.autoInc.insert(chat)
//     //    }
//     //}  

//   }

//   def reroute(chat: Chat) : Boolean = {
//     val chatHolder = new ChatHolder(chat)

//     chat match {
//       // Attempt to send Private chat
//       case PrivateChat(text, sender, receiver) =>
//         typeOfChat = "private"
//         val targetFuture = context.system.actorSelection("user/SockoSender" + receiver.id).resolveOne(100.milliseconds)
//         val targetRef = Await.result(targetFuture, 100.milliseconds)
//         if(targetRef.isTerminated) {
//           return false
//         } else {
//           targetRef ! chatHolder
//           return true
//         }
//       // Send Public Chat to every chat sender
//       case PublicChat(text, sender) =>
//         typeOfChat = "public"
//         val actorSelection = context.system.actorSelection("user/SockoSender*")

//         actorSelection ! new ConnectionWrite("{\"type\": \"chat\", \"sender\": \"" + sender.username + "\", \"message\": " + text + "}\n")

//         return true
//     }
//   }
// }
