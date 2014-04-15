package ayai.components

/** Crane Imports **/
import crane.Component
import ayai.statuseffects._
/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.{ArrayBuffer, HashMap}

// Added tempValues so we can remove any effects from a  given stat
case class Stat(attributeType: String, magnitude: Int) {
  var cachedValue: Int = 0
  var modifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  def asJson: JObject = {
    (attributeType -> magnitude)
  }

  /*
    Will first check if to process the effect again, and if invalid then remove the effect
  */
  def updateCachedValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    cachedValue = magnitude
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
      } 
      else if(!isAbsolute) {
        if(effect.isRelative && effect.isValueRelative) {
          cachedValue = cachedValue + effect.process(cachedValue)
        } 
      }
    }
    return cachedValue
  }

  def getValue(): Int = {
    cachedValue
  }
}

class Stats(val stats: ArrayBuffer[Stat]) extends Component {

  def addStat(newStat: Stat) = {
    stats += newStat
  }

  def addStat(attributeType: String, magnitude: Int) = {
    stats += Stat(attributeType, magnitude)
  }

  //Removes all stats which match the statName
  //hopefully should only ever be 1 match
  def removeStat(statName: String) = {
    stats.remove(stats.indexWhere((stat: Stat) => stat.attributeType == statName))
  }

  def getAttributeByType(attributeType: String): Stat = {
    for(stat <- stats) {
      if(stat.attributeType == attributeType) {
        return stat
      }
    }
    return new Stat("", 0)
  }

  def getValueByAttribute(attributeType: String): Int = {
    for(stat <- stats) {
      if(stat.attributeType == attributeType) {
        return stat.getValue()
      }
    }
    return 0    
  }

  def updateCachedValue() {
    for(stat <- stats) {
      stat.updateCachedValue
    }
  }

  override def toString = {
      "STAT"
//    "Stat: " + stats(0).attributeType + ", magnitude: " + stats(0).magnitude
  }
}
