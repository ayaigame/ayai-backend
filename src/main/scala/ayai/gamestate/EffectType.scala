package ayai.gamestate

sealed class EffectType 
	case class Percentage extends EffectType
	case class Addition extends EffectType
	case class Multiplier extends EffectType
	case class TimedInterval extends EffectType
