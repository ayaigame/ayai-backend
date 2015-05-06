/*
package ayai.persistence

/** External Imports **/
import collection.mutable.Stack
import org.scalatest._
import scala.slick.driver.H2Driver.simple._

class UserSpec extends FlatSpec with Matchers {

  "A User" should "be created by NewUser" in {
    var newUser = NewUser("tim", "tim")
    var user = new User(1, "tim", "tim")

    newUser.realname should equal(user.realname)
    newUser.username should equal(user.username)
  }

  it should "Be saved to the database" in {
    Database.forURL("jdbc:h2:mem:usertest", driver = "org.h2.Driver") withSession { implicit session:Session =>
      Users.ddl.create
      var newUser = NewUser("tim", "tim")

      Users.autoInc.insert(newUser)

      val query = for {
        u <- Users 
      } yield (u.id, u.realname, u.username)

      val queriedUser = query.firstOption map {case (id, username, realname) => User(id, username, realname) }

      queriedUser match {
        case None =>
          println("This state is a failure")
          1 should equal(2)
        case Some(q) =>
          q.realname should equal(newUser.realname)
          q.username should equal(newUser.username)
      }
    }
  }
}
*/