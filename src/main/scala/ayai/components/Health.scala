package ayai.components

/** Crane Imports **/
import crane.Component
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ayai.statuseffects._

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Health(var currentHealth: Int, var maximumHealth: Int) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)
  var currentModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var maxModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var currentCached: Int = currentHealth
  var maxCached: Int = maximumHealth  
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
    updateCurrentValue()
    updateMaxValue()
  }

  def updateMaxValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    maxCached = maximumHealth

    for(effect <- maxModifiers) {
      if(!effect.isValid) {
        maxModifiers -= effect
      } else {
        if(!effect.isRelative) {
          isAbsolute = true
          absoluteValue = effect
          maxCached = effect.effectiveValue
        }
      }
    }

    for(effect <- maxModifiers) {
      if(isAbsolute && absoluteValue != effect) {
        if(effect.isRelative && !effect.isValueRelative) {
          maxCached = maxCached + effect.effectiveValue
        }
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && !effect.isValueRelative) {
          maxCached = maxCached + effect.effectiveValue
        } 
      }
    }
    for(effect <- maxModifiers) {
      if(isAbsolute && absoluteValue != effect) {
        if(effect.isRelative && effect.isValueRelative) {
          maxCached = maxCached + effect.process(maxCached)
        }
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && effect.isValueRelative) {
          maxCached = maxCached + effect.process(maxCached)
        } 
      }
    }
  }

  def updateCurrentValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    currentCached = currentHealth

    for(effect <- currentModifiers) {
      if(!effect.isValid) {
        currentModifiers -= effect
      } else {
        if(!effect.isRelative) {
          isAbsolute = true
          absoluteValue = effect
          currentCached = effect.effectiveValue
        }
      }
    }

    for(effect <- currentModifiers) {
      if(isAbsolute && absoluteValue != effect) {
        if(effect.isRelative && !effect.isValueRelative) {
          currentCached = currentCached + effect.effectiveValue
        }
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && !effect.isValueRelative) {
          currentCached = currentCached + effect.effectiveValue
        } 
      }
    }
    for(effect <- currentModifiers) {
      if(isAbsolute && absoluteValue != effect) {
        if(effect.isRelative && effect.isValueRelative) {
          currentCached = currentCached + effect.process(currentCached)
        }
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && effect.isValueRelative) {
          currentCached = currentCached + effect.process(currentCached)
        } 
      }
    }
  }

  def getCurrentValue(): Int = {
    currentCached
  }
  def getMaxValue(): Int = {
    maxCached
  }
}
