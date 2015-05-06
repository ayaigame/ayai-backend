/*
package ayai.persistence

/** External Imports **/
import collection.mutable.Stack
import org.scalatest._
import scala.slick.driver.H2Driver.simple._

class StoredMessageSpec extends FlatSpec with Matchers {

  "A StoredMessage" should "should be created by NewStoredMessage" in {
    var newStoredMessage = NewStoredMessage("hello", 1, 1, false)
    var storedMessage = new StoredMessage(1, "hello", 1, 1, false)
    
    newStoredMessage.message should equal(storedMessage.message)
    newStoredMessage.senderID should equal(storedMessage.senderID)
    newStoredMessage.receiverID should equal(storedMessage.receiverID)
    newStoredMessage.received should equal(storedMessage.received)
  }

  it should "Be saved to the database" in {
    Database.forURL("jdbc:h2:mem:storedmessagetest", driver = "org.h2.Driver") withSession { implicit session:Session =>
      StoredMessages.ddl.create
      var newStoredMessage = NewStoredMessage("hello", 1, 1, false)

      StoredMessages.autoInc.insert(newStoredMessage)

      val query = for {
        s <- StoredMessages 
      } yield (s.id, s.message, s.senderID, s.receiverID, s.received)

      val queriedMessage = query.firstOption map {case (id, message, senderID, receiverID, received) => StoredMessage(id, message, senderID, receiverID, received) }

      queriedMessage match {
        case None =>
          println("This state is a failure")
          1 should equal(2)
        case Some(q) =>
          q.message should equal(newStoredMessage.message)
          q.senderID should equal(newStoredMessage.senderID)
          q.receiverID should equal(newStoredMessage.receiverID)
          q.received should equal(newStoredMessage.received)
      }
    }
  }
}
*/
