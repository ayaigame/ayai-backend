package ayai.components

/** Crane Imports **/
import crane.{Component, Entity}
import scala.collection.mutable._

/** External Imports **/
import net.liftweb.json.Serialization.write
import net.liftweb.json._

class GenerateQuest(val initiator: Entity, val recipient: Entity) extends Component {
}
