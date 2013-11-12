package ayai.main.networking.messaging

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

case class PublicMessage(m: String, s: String) extends Message {
  val message = m
  val sender = s

  private def store: Boolean = {
    // DB Stuff
    Database.forURL("jdbc:h2:mem:ayai", driver = "org.h2.Driver") withSession {
      StoredMessages.insert(StoredMessage(None, message, sender, "ALL", true))
    }


    // Successfully stored
    true
  }

  def send: Boolean = {
    //val broadCaster = getBroadcast
    //broadCaster.sendMessage(message, sender)
    false
  }

//  def getBroadcast: Broadcast = {
    // find the server's broadcast
    
// }
}
