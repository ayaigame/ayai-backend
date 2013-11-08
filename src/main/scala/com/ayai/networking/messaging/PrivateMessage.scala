package com.ayai.main.networking.messaging

import akka.actor._
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

case class PrivateMessage(m: String, s: String, r: String, a: ActorRef) extends Message {
  val message = m
  val sender = s
  val receiver = r
  val sendingActor = a
  
  private def store(received: Boolean): Boolean = {
    // Start DB
    Database.forURL("jdbc:h2:mem:ayai", driver = "org.h2.Driver") withSession {
      StoredMessages.insert(StoredMessage(None, message, sender, receiver, received))
    }
    true
  }

  def send: Boolean = { 
    // Find other person
    val otherPlayer = findPlayer
    var received = true

	if(otherPlayer != null){
      otherPlayer ! ReceiveMessage(message, sender)
	} else {
      received = false
	}

    // If can't find, store
	if(!store(received)){
      // If message isn't stored, raise exception
      false
	}
    true 
  }

  def findPlayer: ActorRef = {
    // If you find ActorRef
    null
  }
	
}
