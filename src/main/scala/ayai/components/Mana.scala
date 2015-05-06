package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ayai.statuseffects._

case class Mana(var currentMana: Int, var maximumMana: Int, growth: Int = 10) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)
  
  private var currentModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  private var maxModifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]

  private var currentCached: Int = 0
  private var maxCached: Int = 0

  def addDamage(damage: Float) {
    currentMana -= damage.toInt
    if (currentMana < 0) {
      currentMana = 0
    }
  }

  def levelUp() {
    maximumMana += growth
  }

  def updateCachedValue() {
    updateCurrentValue()
    updateMaxValue()
  }
  
  def updateMaxValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    maxCached = maximumMana

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

  def updateCurrentValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    currentCached = currentMana

    for (effect <- currentModifiers) {
      if (!effect.isValid) {
        currentModifiers -= effect
      } else {
        if (!effect.isRelative) {
          isAbsolute = true
          absoluteValue = effect
          currentCached = effect.effectiveValue
        }
      }
    }

    for (effect <- currentModifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isRelative && !effect.isValueRelative) {
          currentCached = currentCached + effect.effectiveValue
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && !effect.isValueRelative) {
          currentCached = currentCached + effect.effectiveValue
        } 
      }
    }

    for (effect <- currentModifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isRelative && effect.isValueRelative) {
          currentCached = currentCached + effect.process(currentCached)
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && effect.isValueRelative) {
          currentCached = currentCached + effect.process(currentCached)
        } 
      }
    }
  }

  def getCurrentValue: Int = currentCached

  def getMaxValue: Int = maxCached

  def addEffect(effect: Effect) {
    effect.effectType match {
      case "currentMana" => currentModifiers += effect
      case "maxMana" => maxModifiers += effect
      case _ =>
    }
  }

  override def toString : String = {
    write(this)
  }

  implicit def asJson() : JObject = {
    "Mana" ->
      ("currMana" -> currentCached) ~
      ("maximumMana" -> maxCached) ~
      ("currentEffects" -> currentModifiers.map{ce => ce.asJson}) ~
      ("maximumEffects" -> maxModifiers.map{me => me.asJson})
  }

}
