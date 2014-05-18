package ayai.components

import crane.Component
import ayai.apps.Constants
import ayai.statuseffects._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.collection.mutable.{ArrayBuffer, HashMap}

case class Experience(var baseExperience: Long, var level: Int) extends Component {
  var modifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var cachedValue: Int = baseExperience.toInt

  def levelUp(experienceThreshold: Long): Boolean = {
    if(baseExperience >= experienceThreshold) {
      level += 1
      return true
    }
    return false
  }

  def updateCachedValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    cachedValue = baseExperience.toInt
    
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
  }

  def getValue(): Int = {
    cachedValue
  }

  def addEffect(effect: Effect) {
    effect.effectType match {
      case "experience" => modifiers += effect
      case _ => 
        /// print error 
    } 
  }

  def asJson() : JObject = {
    ("experience" -> 
      ("baseExperience" -> baseExperience) ~
      ("maxExperience" -> Constants.EXPERIENCE_ARRAY(level-1)) ~
      ("level" -> level) ~
      ("currentEffects" -> (modifiers.map{e => e.asJson})))
  }
}