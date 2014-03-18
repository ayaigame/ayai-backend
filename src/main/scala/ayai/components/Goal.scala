package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

trait Intent

case class MoveTo(position: Position) extends Intent {
  override def toString: String = {
    s"Move To $position" 
  }
}

case class AttackTo(position: Position) extends Intent {
  override def toString: String = {
    s"Attack To $position" 
  }
}

class Goal extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  var goal: Intent = new MoveTo(new Position(0, 0))

  override def toString: String = {
    write(this)
  }

  //implicit def asJson: JObject = {
  //  ("goal" -> 
  //    ("goal" -> goal))
  //}
}
