package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Mana(var currentMana: Int, var maximumMana: Int) extends Component{
implicit val formats = Serialization.formats(NoTypeHints)
  def getCurrentMana() : Int = {
    return this.currentMana
  }
  
  def setCurrentMana(characterMana : Int){
    this.currentMana = characterMana
  }
  
  def getMaximumMana() : Int = {
    return this.maximumMana
  }
  
  def setMaximumMana(maximumMana : Int) {
    this.maximumMana = maximumMana
  }
  
  def isAlive : Boolean = {
    return currentMana > 0
  }
  
  def addDamage(damage:Float) {
    currentMana.-(damage) 
    if(currentMana < 0) {
      currentMana = 0
    }
  }

  override def toString : String = {
    write(this)
  }

  implicit def asJson() : JObject = {
    ("Mana" -> 
      ("currMana" -> currentMana) ~
      ("maximumMana" -> maximumMana))
  }

}
