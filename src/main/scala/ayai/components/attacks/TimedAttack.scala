package ayai.components.attacks

import crane.Component

class TimedAttack(damage : Int, msActive : Int, startTime : Long ) extends Attack(damage) {
	def isReady(endTime : Long) : Boolean = {
		if(endTime - startTime >= msActive) {
			return true
		}
		return false
	}
}