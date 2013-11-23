package ayai.persistence
/**
 * ayai.persistence.User
 * Database object for storing Users
 * For information on User vs New User: http://stackoverflow.com/questions/13199198/using-auto-incrementing-fields-with-postgresql-and-slick
 */

/** External Imports */
import scala.slick.driver.H2Driver.simple._

case class User(id: Int, username: String, realname: String)
case class NewUser(username: String, realname: String)

object Users extends Table[User]("users") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def realname = column[String]("realname")

  def * = id ~ username ~ realname <> (User, User.unapply _)
  def autoInc = username ~ realname <> (NewUser, NewUser.unapply _) returning id
}

object UserQuery {
  def getByID(id: Int):Option[User] = {
    Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
      val query = for{u <- Users if u.id is id} yield (u.id, u.username, u.realname)
      query.firstOption map { case(i, u, r) => User(i, u, r) }
    }
  }

  def getByUsername(username: String):Option[User] = {
    Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
      val query = for{u <- Users if u.username is username} yield (u.id, u.username, u.realname)
      query.firstOption map { case(i, u, r) => User(i, u, r) }
    }
  }
}
