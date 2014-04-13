package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Health(var currentHealth: Int, var maximumHealth: Int) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)
  var modifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var cachedCurrentHealth: Int = currentHealth
  var cachedMaximumHealth: Int = maximumHealth  
  def isAlive: Boolean = currentHealth > 0

  def addDamage(damage: Float) {
    currentHealth -= damage.toInt
    if(currentHealth < 0) {
      currentHealth = 0
    }
  }

  def refill() { currentHealth = maximumHealth }

  override def toString: String = {
    write(this)
  }

  implicit def asJson(): JObject = {
    ("health" ->
      ("currHealth" -> currentHealth) ~
      ("maximumHealth" -> maximumHealth))
  }
  /*
    Will first check if to process the effect again, and if invalid then remove the effect
  */
  def updateCachedValue() {
    var isCurrentAbsolute: Boolean = false 
    var currentAbsoluteValue: Effect = null
    var isMaxAbsolute: Boolean = false 
    var maxAbsoluteValue: Effect = null

    for(effect <- modifiers) {
      if(!effect.isValid) {
        modifiers -= effect
      } else {

        if(!effect.isRelative) {
          isAbsolute = true
          absoluteValue = effect
          cachedValue = effect.effectiveValue
        }
      }
    }

    for(effect <- modifiers) {
      if(isAbsolute && absoluteValue != effect) {
        if(effect.isRelative && !effect.isValueRelative) {
          cachedValue = cachedValue + effect.effectiveValue
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && !effect.isValueRelative) {
          cachedValue = cachedValue + effect.effectiveValue
        } 
      }
    }
    for(effect <- modifiers) {
      if(isAbsolute && absoluteValue != effect) {
        if(effect.isRelative && effect.isValueRelative) {
          cachedValue = cachedValue + effect.process(cachedValue)
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && effect.isValueRelative) {
          cachedValue = cachedValue + effect.process(cachedValue)
        } 
      }
    }
    return cachedValue
  }

  def getValue() {
    cachedValue
  }
}
