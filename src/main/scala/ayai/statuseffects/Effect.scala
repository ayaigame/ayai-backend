package ayai.statuseffects

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

// isRelative means that the effect should take the place of the existing value of the stat/health
// isValueRelative means that the effect should use the stats value with the multiplier
case class Effect(id: Int,
                  name: String,
                  description: String,
                  effectType: String,
                  private val value: Int,
                  attribute: TimeAttribute,
                  multiplier: Multiplier,
                  isRelative: Boolean = true,
                  isValueRelative: Boolean = false) {

  // TODO reduce mutability or make thread-safe
  var effectiveValue: Int = 0
  var imageLocation: String = ""

  // first get the value and use it with the additive
  def initialize() {
    effectiveValue = multiplier.process(value)
  }

  def process(effectValue: Int = 0): Int = {
    updateValue(value)
    if(attribute.isReady) {
      attribute.process()
      if(isValueRelative) {
        updateValue(effectValue)
      } else {
        effectiveValue
      }
    } 
    0
  }

  def updateValue(value: Int) {
    effectiveValue = multiplier.process(value)
  }

  def isReady(): Boolean = {
    attribute.isReady
  }
  def isValid(): Boolean = {
    attribute.isValid
  }

  def asJson(): JObject = {
    ("effect" ->
      ("effecttype" -> effectType) ~
      ("effectiveValue" -> effectiveValue) ~
      (attribute.asJson()) ~
      ("image" -> imageLocation))
  }
}