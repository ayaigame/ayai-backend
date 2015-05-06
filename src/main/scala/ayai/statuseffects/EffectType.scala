package ayai.statuseffects

/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class EffectType(val effectType: String) {
  def asJson(): JObject = {
    "effecttype" -> effectType
  }
}
