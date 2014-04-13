package ayai.statuseffects

case class Additive(multiplier: Double) {

	/**
	Examples - if you want to add %5 of something do value(1) * .05
	Or if you want to subtract %5 of something do value(1) * -.05
	**/
	def process(value: Double): Double = {
		return value * multiplier
	}

}