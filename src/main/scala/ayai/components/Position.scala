package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._

// conventions:
// x is always >= 0 and increases to the right
// y is always >= 0 and increases downwards
case class Position(var x: Int, var y: Int) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit def asJson: JObject = {
    "position" -> ("x" -> x) ~ ("y" -> y)
  }

  override def toString: String = write(this)
}
