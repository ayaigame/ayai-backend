package ayai.gamestate

class EffectType extends Enumeration {
	type EffectType = Value

	//percentage - add percentage of value to attribute
	val Percentage = Value("Percentage")
	//multiplier - multiple value of attribute and add to it
	val Multiplier = Value("Multiplier")
	//addition - add value to attribute
	val Addition = Value("Addition")
	//for a timed period add value to attribute
	val TimedInterval = Value("Timed")
}