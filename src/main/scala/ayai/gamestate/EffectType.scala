package ayai.gamestate

class EffectType extends Enumeration {
	type EffectType = Value

	//percentage - add percentage of value to attribute
	val Percentage = EffectType("Percentage")
	//multiplier - multiple value of attribute and add to it
	val Multiplier = EffectType("Multiplier")
	//addition - add value to attribute
	val Addition = EffectType("Addition")
	//for a timed period add value to attribute
	val TimedInterval = EffectType("Timed")
}