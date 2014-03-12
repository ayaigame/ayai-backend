package ayai.gamestate

class EffectType {
	def process(value: Float, attribute: String) {}
}
case class Percentage extends EffectType {
	override def process(value: Float, attribute: String){}

}
case class Addition() extends EffectType {
	override def process(value: Float, attribute: String){}

}
case class Multiplier() extends EffectType {
	override def process(value: Float, attribute: String) {}

}

case class TimedInterval(startTime: Long, timeInterval: Long) extends EffectType {
	override def process(value: Float, attribute: String){}
}
