package ayai.networking

/** Ayai Imports **/
// import ayai.networking.Connection

/** Akka Imports **/
import akka.actor.Actor

class NetworkMessageInterpreter extends Actor {
  def interpretMessage = {
    println("Interpret message")
  }

  def receive = {
    case InterpretMessage() => interpretMessage 
    case _ => println("Error: incomprehensible message.")
  }
}