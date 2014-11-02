package ayai.components

/** Crane Imports **/
import crane.Component
import scala.collection.mutable.ArrayBuffer
import ayai.statuseffects._

/** External Imports **/
import net.liftweb.json.Serialization.write
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
  currentHealth and maximumHealth should not be touched as they are need to determine what health should be set back to if effect is temporary
**/
case class Health(var currentHealth: Int, var maximumHealth: Int, growth: Int = 20) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)
  var currentModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var maxModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var currentCached: Int = currentHealth
  var maxCached: Int = maximumHealth  
  def isAlive: Boolean = currentHealth > 0

  def addDamage(damage: Float) {
    currentCached -= damage.toInt
    if(currentCached <= 0) {
      currentCached = 0
      currentHealth = 0
    }
  }

  def levelUp() {
    maximumHealth += growth
    maxCached = maximumHealth
  }

  def refill() { 
    currentHealth = maximumHealth 
    currentCached = currentHealth
  }

  override def toString: String = {
    write(this)
  }

  implicit def asJson(): JObject = {
    "health" ->
      ("currHealth" -> currentCached) ~
      ("maximumHealth" -> maxCached) ~
      ("currentEffects" -> currentModifiers.map{ce => ce.asJson}) ~
      ("maximumEffects" -> maxModifiers.map{me => me.asJson})
  }

  /*
    Will first check if to process the effect again, and if invalid then remove the effect
  */

  def updateCachedValue() {
    updateCurrentValue()
    updateMaxValue()
  }

  // update the cached value of the maxHealth effects
  def updateMaxValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    val invalidItems = new ArrayBuffer[Effect]()

    // TODO this can definitely be simplified considerably
    for (effect <- maxModifiers) {
      if (!effect.isValid) {
        invalidItems += effect
      } else {
        if (!effect.isRelative) {
          isAbsolute = true
          if (effect.isReady) {
            effect.process(maximumHealth)
            absoluteValue = effect
            maxCached = effect.effectiveValue

          }
        } else {
          maxCached = maximumHealth
        }
      }
    }

    for (effect <- invalidItems) {
      maxModifiers -= effect
    }

    for (effect <- maxModifiers) {
      if (!effect.isValid) {
        maxModifiers -= effect
      } else {
        if (!effect.isRelative) {
          isAbsolute = true
          absoluteValue = effect
          maxCached = effect.effectiveValue
        }
      }
    }

    for (effect <- maxModifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isRelative && !effect.isValueRelative) {
          maxCached = maxCached + effect.effectiveValue
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && !effect.isValueRelative) {
          maxCached = maxCached + effect.effectiveValue
        } 
      }
    }

    // TODO is this second loop really needed?
    for (effect <- maxModifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isRelative && effect.isValueRelative) {
          maxCached = maxCached + effect.process(maxCached)
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && effect.isValueRelative) {
          maxCached = maxCached + effect.process(maxCached)
        } 
      }
    }
  }

  // update the cached value of the currentHealth effects
  def updateCurrentValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    val invalidItems = new ArrayBuffer[Effect]()
    for (effect <- currentModifiers) {
      if (!effect.isValid) {
        invalidItems += effect
      } else {
        if (!effect.isRelative) {
          isAbsolute = true
          if (effect.isReady) {
            effect.process(currentHealth)
            absoluteValue = effect
            currentCached = effect.effectiveValue
          }
        }
      }
    }

    for (effect <- invalidItems) {
      currentModifiers -= effect
    }

    for (effect <- currentModifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (!effect.isValueRelative) {
          effect.process(currentHealth)
          currentCached = currentCached + effect.effectiveValue
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && !effect.isValueRelative) {
          effect.process(currentHealth)
          currentCached = currentCached + effect.effectiveValue
        } 
      }
    }

    for (effect <- currentModifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isValueRelative) {
          effect.process(currentHealth)
          currentCached = currentCached + effect.process(currentCached)
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && effect.isValueRelative) {
          effect.process(currentHealth)
          currentCached = currentCached + effect.process(currentCached)
        } 
      }
    }
  }

  def getCurrentValue: Int = currentCached

  def getMaxValue: Int = maxCached

  def addEffect(effect: Effect) {
    effect.effectType match {
      case "currentHealth" => currentModifiers += effect
      case "maxHealth"     => maxModifiers += effect
      case _ => println(effect.effectType)
    }
  }  
}
