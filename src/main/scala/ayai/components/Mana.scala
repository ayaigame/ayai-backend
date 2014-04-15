package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ayai.statuseffects._

case class Mana(var currentMana: Int, var maximumMana: Int) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)
  
  var currentModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var maxModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]

  var currentCached: Int = 0
  var maxCached: Int = 0

  def addDamage(damage: Float) {
    currentMana -= damage.toInt
    if(currentMana < 0) {
      currentMana = 0
    }
  }
  def updateCachedValue() {
    updateCurrentValue()
    updateMaxValue()
  }
  
  def updateMaxValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    maxCached = maximumMana

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
    currentCached = currentMana

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
  override def toString : String = {
    write(this)
  }

  implicit def asJson() : JObject = {
    ("Mana" ->
      ("currMana" -> currentCached) ~
      ("maximumMana" -> maxCached))
  }

}
