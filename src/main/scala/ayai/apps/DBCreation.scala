package ayai.apps

/**
 * ayai.apps.DBCreation
 * Creates DB
 */

/** Ayai Imports **/
import ayai.persistence._

/** External Imports **/
import java.nio.file.{Files, Paths}
import scala.slick.driver.H2Driver.simple._

object DBCreation {
  def main(args: Array[String]) = {
    // If DB Doesn't exist, create it
    if(!Files.exists(Paths.get("ayai.h2.db"))) {
      Database.forURL("jdbc:h2:file:ayai", driver = "org.h2.Driver") withSession { implicit session:Session =>
        (Users.ddl ++ StoredChats.ddl).create
        // Create Users
        val names = Array("tim", "kurt", "rob", "ryan", "josh", "jarrad", "jared", "user")
        for (name <- names) {
          var newUser = NewUser(name, name)
          Users.autoInc.insert(newUser) 
        }
      }
  
    }
  }
}
