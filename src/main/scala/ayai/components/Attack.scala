package ayai.components

/** Crane Imports **/
import crane.{Component, Entity}
import scala.collection.mutable._

/** External Imports **/
import net.liftweb.json.Serialization.write
import net.liftweb.json._

class Attack(val initiator: Entity,
             val victims: ArrayBuffer[Entity] = new ArrayBuffer[Entity](),
             val attacked: ArrayBuffer[Entity] = new ArrayBuffer[Entity]) extends Component {

  implicit val formats = Serialization.formats(NoTypeHints)

  def removeVictims() {
    victims.clear()
  }

  def addVictim(e: Entity) {
    victims += e
  }

  def moveVictims() {
    for (victim <- victims) {
      if (!attacked.contains(victim)) {
        attacked += victim
      }
    }

    removeVictims()
  }

  override def toString: String = {
    write(this)
  }
}
