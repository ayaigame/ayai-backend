package ayai.statuseffects

import crane.{Entity}

/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

abstract class EffectType(type: String) {
	def process(entity: Entity, value: Double, attribute: Attribute)
	def getType()
	def asJson(): JObject
}

// This will be the buffs and debuffs to a stat, is temporary
class StatChange(val statToEffect: String) extends EffectType(statToEffect) {
	override def process(entity: Entity, value: Double, attribute: Attribute) = {
		entity.getComponent(classOf[Stats]) match {
			case Some(stats: Stats) => 
				val stat = stats.getAttributeByType(statToEffect)
					return stat
				case _ => 
				// print out error
		}
	}

	override def getType(): String = {
		type
	}
}
