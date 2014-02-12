package ayai.components

import crane.Component
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Health(var currentHealth: Int, var maximumHealth: Int) extends Component{
implicit val formats = Serialization.formats(NoTypeHints)
  def getCurrentHealth() : Int = {
    return this.currentHealth
  }
  
  def setCurrentHealth(characterHealth : Int){
    this.currentHealth = characterHealth
  }
  
  def getMaximumHealth() : Int = {
    return this.maximumHealth
  }
  
  def setMaximumHealth(maximumHealth : Int) {
    this.maximumHealth = maximumHealth
  }
  
  def isAlive : Boolean = {
    return currentHealth > 0
  }
  
  def addDamage(damage:Float) {
    currentHealth.-(damage) 
    if(currentHealth < 0) {
      currentHealth = 0
    }
  }

  def refill() {
    currentHealth = maximumHealth
  }

  override def toString : String = {
    write(this)
  }

  implicit def asJson() : JObject = {
    ("health" -> 
      ("currHealth" -> currentHealth) ~
      ("maximumHealth" -> maximumHealth))
  }

}