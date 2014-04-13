package ayai.components

import crane.Component
import ayai.statuseffects._
/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
case class StatusEffectBag(val statusEffects: ArrayBuffer[Effect] = new ArrayBuffer[Effect]()) extends Component {
	def removeStatusEffect(statusEffect: Effect) {
		statusEffects -= statusEffect
	}

	def addStatus(statusEffect: Effect) {
		statusEffects += statusEffect
	}


	def asJson(): JObject = {
		("statuseffects" -> statusEffects.map{se => se.asJson})
	}
}