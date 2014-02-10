package ayai.components

import crane.Component

import ayai.gamestate.Effect
import scala.collection.mutable.ArrayBuffer
/*
* This is the item class essentially, will tell whether an item 
* can be held by a character
*/
class Containable(var id : Int, var name : String, var effects : ArrayBuffer[Effect] ) extends Component {
	
	def addEffect(effect : Effect) {
		effects += effect
	}

}