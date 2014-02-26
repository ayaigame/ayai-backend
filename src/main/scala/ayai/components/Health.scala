package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Health(var currentHealth: Int, var maximumHealth: Int) extends Component{
implicit val formats = Serialization.formats(NoTypeHints)
  
  def isAlive: Boolean = currentHealth > 0
  
  def addDamage(damage: Float) {
    currentHealth -= damage.toInt
    if(currentHealth < 0) {
      currentHealth = 0
    }
  }

  def refill() {
    currentHealth = maximumHealth
  }

  override def toString: String = {
    write(this)
  }

  implicit def asJson(): JObject = {
    ("health" -> 
      ("currHealth" -> currentHealth) ~
      ("maximumHealth" -> maximumHealth))
  }


}
