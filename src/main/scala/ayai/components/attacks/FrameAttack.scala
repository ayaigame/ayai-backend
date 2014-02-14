package ayai.components.attacks

class FrameAttack(damage : Int,var framesActive : Int,var framesCount : Int = 0) extends Attack(damage) {
	def isReady() : Boolean = {
		if(framesActive <= framesCount) {
			return true
		}
		return false
	}
}