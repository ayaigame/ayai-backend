package ayai.gamestate

import crane.Entity

/*
 * This class will define the type of effect being created 
 * Must specify the type, attribute to be effected and value
 * Example Cure - EffectType(Addition), Attribute(Health), value(50)))
 * This will add 50 health to health attribute
 */
class StatusEffect (val etype: EffectType, var attribute: String, var value: Float) {
	def process(e: Entity) {
    etype.process(value, attribute)
		
	}
}
