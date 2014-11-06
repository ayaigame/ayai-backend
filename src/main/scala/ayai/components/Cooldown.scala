package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._

case class Cooldown(var startTime: Long, var length: Long) extends Component {

  implicit val formats = Serialization.formats(NoTypeHints)

  def isReady: Boolean =  (System.currentTimeMillis() - startTime) > length

  implicit def asJson: JObject = {
    "cooldown" ->
      ("length" -> length) ~
      ("startTime" -> startTime)
  }

  override def toString: String = { write(this) }
}