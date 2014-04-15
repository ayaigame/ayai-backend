package ayai.components

/** Crane Imports **/
import crane.Component
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ayai.statuseffects._

class Velocity (var x: Int, var y: Int) extends Component {
  var modifiers: ArrayBuffer[Effect] = new ArrayBuffer[Effect]
  var cachedValue: Int = 0

  def updateCachedValue() {
    var isAbsolute: Boolean = false 
    var absoluteValue: Effect = null
    cachedValue = x
    
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
}
