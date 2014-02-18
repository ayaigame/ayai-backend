package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class Attack(var damage: Int) extends Component {
  var infinite : Boolean = false
  implicit val formats = Serialization.formats(NoTypeHints)
  
  implicit def asJson() : JObject = {
    ("attackDamage" -> damage)
  }

  override def toString: String = {
    write(this)
  }
}
