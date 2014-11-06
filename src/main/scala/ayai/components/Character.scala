package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.write
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class Character(id: String, name: String) extends Component{
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit def asJson() : JObject = {
    ("id" -> id) ~
    ("name" -> name) 
  }

  override def toString: String = {
    write(this)
  }
}
