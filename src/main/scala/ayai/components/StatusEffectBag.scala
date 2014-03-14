package ayai.components

import crane.Component
import ayai.gamestate.{StatusEffect, EffectType}
import scala.collection.mutable._
case class StatusEffectBag(val statusEffects: ArrayBuffer[StatusEffect] = new ArrayBuffer[StatusEffect]()) extends Component {
	def removeStatusEffect(statusEffect: StatusEffect) {
		statusEffects -= statusEffect
	}

	def addStatus(statusEffect: StatusEffect) {
		statusEffects += statusEffect
	}

	
}