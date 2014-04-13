package ayai.statuseffects

import crane.Entity

/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class Effect(effectType: EffectType, value: Double, attribute: Attribute, additive: Additive) {
	
	var effectiveValue: Double = 0
	var imageLocaton: String = ""

	// first get the value and use it with the additive
	def initialize() {
		effectiveValue = additive.process(value)
	}

	def process(entity: Entity) {
		effectType.process(entity, effectiveValue, attribute)
	}

	def updateValue(value: Double) {
		effectiveValue = additive.process(value)
	}

	def isValid(): Boolean = {
		attribute.isValid
	}

	def asJson(): JObject = {
		("effect" ->
			(effectType.asJson()) ~
			("effectiveValue" -> effectiveValue) ~
			(attribute.asJson()) ~
			("image" -> image))
	}
}