package ayai.components

/** Crane Imports **/
import crane.{Component, Entity}
import scala.collection.mutable._

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class Attack(var initiator: Entity, 
             var victims: ArrayBuffer[Entity] = new ArrayBuffer[Entity](),
             var attacked: ArrayBuffer[Entity] = new ArrayBuffer[Entity]) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  def removeVictims() {
  	victims = new ArrayBuffer[Entity]()
  }

  def addVictim(e: Entity) {
  	victims += e
  }

  def moveVictims() {
    for(victim <- victims) {
      if(!attacked.contains(victim)) 
        attacked += victim
    }
    removeVictims
  }

  override def toString: String = {
    write(this)
  }
}
