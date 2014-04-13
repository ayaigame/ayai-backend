package ayai.statuseffects

import crane.{Entity}

/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class EffectType(effecttype: String) {

	def getType(): String = {
		effecttype
	}

	def asJson(): JObject = {
		("effecttype" -> effecttype)
	}

}
