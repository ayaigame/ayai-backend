package ayai.gamestate

sealed class EffectType {
	def process(value: Float, attribute: Class)
}
case class Percentage extends EffectType {
	def process(value: Float, attribute: Class){}

}
case class Addition() extends EffectType {
	def process(value: Float, attribute: Class){}

}
case class Multiplier() extends EffectType {
	def process(value: Float, attribute: Class) {}

}

case class TimedInterval(startTime: Long, timeInterval: Long) extends EffectType {
	def process(value: Float, attribute: Class)
}
