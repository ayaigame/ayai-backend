package ayai.gamestate

sealed class EffectType 
case object Percentage extends EffectType
case object Addition extends EffectType
case object Multiplier extends EffectType
case object TimedInterval extends EffectType
