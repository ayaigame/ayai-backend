package ayai.components

/** Crane Imports **/
import crane.{Component, Entity}

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class Attack(var initiator: Entity) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  override def toString: String = {
    write(this)
  }
}
