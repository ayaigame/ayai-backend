package ayai.components

/** Crane Imports **/
import crane.{Component, Entity}

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class Attack(var initiator: Entity, var victims: ArrayList[Entity] = new ArrayList[Entity]()) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  def removeVictims() {
  	victims = new ArrayList[Entity]()
  }

  def addVictim(e: Entity) {
  	victims += e
  }

  override def toString: String = {
    write(this)
  }
}
