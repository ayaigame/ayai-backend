package ayai.components

/** Crane Imports **/
import crane.Component
import ayai.statuseffects._

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.{ArrayBuffer, HashMap}

// Added tempValues so we can remove any effects from a  given stat
case class Stat(attributeType: String, var magnitude: Int, growth: Int) {
  var cachedValue: Int = 0
  var modifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]

  def asJson: JObject = {
    (attributeType -> magnitude) ~
    ("growth" -> growth)
  }

  /*
    Will first check if to process the effect again, and if invalid then remove the effect
  */
  def updateCachedValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    cachedValue = magnitude
    for (effect <- modifiers) {
      if (!effect.isValid) {
        modifiers -= effect
      } else {
        if (!effect.isRelative) {
          isAbsolute = true
          absoluteValue = effect
          cachedValue = effect.effectiveValue
        }
      }
    }

    for (effect <- modifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isRelative && !effect.isValueRelative) {
          cachedValue = cachedValue + effect.effectiveValue
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && !effect.isValueRelative) {
          cachedValue = cachedValue + effect.effectiveValue
        } 
      }
    }

    for (effect <- modifiers) {
      if (isAbsolute && absoluteValue != effect) {
        if (effect.isRelative && effect.isValueRelative) {
          cachedValue = cachedValue + effect.process(cachedValue)
        }
      } else if (!isAbsolute) {
        if (effect.isRelative && effect.isValueRelative) {
          cachedValue = cachedValue + effect.process(cachedValue)
        } 
      }
    }

    cachedValue
  }

  def addEffect(effect: Effect) {
    modifiers += effect
  }

  def removeEffect(effect: Effect) {
    modifiers -= effect
  }

  def getValue(): Int = cachedValue

  def levelUp() {
    magnitude = magnitude + growth
  }
}

class Stats(val stats: ArrayBuffer[Stat] = new ArrayBuffer[Stat]()) extends Component {

  def addStat(newStat: Stat): Unit = {
    stats += newStat
  }

  def addStat(attributeType: String, magnitude: Int, growth: Int): Unit = {
    stats += Stat(attributeType, magnitude, growth)
  }

  //Removes all stats which match the statName
  //hopefully should only ever be 1 match
  def removeStat(statName: String): Unit = {
    stats.remove(stats.indexWhere((stat: Stat) => stat.attributeType == statName))
  }

  def getAttributeByType(attributeType: String): Stat = {
    for (stat <- stats) {
      if (stat.attributeType == attributeType) {
        return stat
      }
    }

    new Stat("", 0, 0)
  }

  def getValueByAttribute(attributeType: String): Int = {
    for (stat <- stats) {
      if (stat.attributeType == attributeType) {
        return stat.getValue()
      }
    }

    0
  }

  def addEffect(effect: Effect) {
    val stat: Stat = getAttributeByType(effect.effectType)
    stat.addEffect(effect)
  }

  def updateCachedValue() {
    stats.foreach(_.updateCachedValue())
  }

  def levelUp() {
    stats.foreach(_.levelUp())
  }

  def asJson(): JObject = {
    "stats" -> stats.map(_.asJson)
  }

  override def toString: String = {
    "STAT"
//    "Stat: " + stats(0).attributeType + ", magnitude: " + stats(0).magnitude
  }
}
