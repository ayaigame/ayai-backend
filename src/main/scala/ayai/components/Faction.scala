package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Faction(name: String) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  override def toString: String = {
    write(this)
  }

  implicit def asJson: JObject = {
    ("faction" ->
      ("name" -> name))
  }
}
