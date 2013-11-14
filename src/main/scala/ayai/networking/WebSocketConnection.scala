// package ayai.networking

// import java.net.{ServerSocket, Socket}
// import java.io._
// import scala.io._
// import scala.util.parsing.json._

// import akka.actor.Actor
// import akka.actor.ActorSystem
// import akka.actor.Props

// class WebSocketConnection(id: Int, s: HookupServerClient) extends Connection(id) {
//   val in = new BufferedSource(s.getInputStream())
//   val out = new PrintStream(s.getOutputStream())

//   def isConnected(): Boolean = {
//     s.isConnected()
//   }

//   def kill() = {
//     s.close()
//   }

//   def read(): String = {
//     //If multiple lines, the rest will be lost...
//     //FIX THIS
//     if (s.isConnected()) {
//       var lines = in.getLines()
//       if(lines.hasNext) {
//         return lines.next() 
//       }
//       else { //IDK why this is necessary but for some reason it won't compile without it.
//         //when we have time we shold figure it out.
//         return ""
//       }
//     }
//     else {
//       return ""
//     }
//   }

//   def write(json: String) = {
//     if (s.isConnected()) {
//       out.println(json)
//       out.flush()
//     }
//   }
// }
