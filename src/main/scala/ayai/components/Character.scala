package ayai.components

import ayai.apps.Constants

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


case class Character(val id: String, val name: String, val experience: Long) extends Component{
	implicit val formats = Serialization.formats(NoTypeHints)

  var level: Int = Constants.EXPERIENCE_ARRAY.indexWhere((exp: Int) => experience < exp)

  implicit def asJson() : JObject = {
    ("id" -> id) ~
    ("name" -> name) ~
    ("experience" -> experience) ~
    ("level" -> level)
  }

  override def toString: String = {
    write(this)
  }
}
