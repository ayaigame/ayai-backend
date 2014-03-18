package ayai.components

import crane.{Component, Entity}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._


/**
** looter: String - the account id of the person who can see it/loot the inventory
** I make it account id based in case we want persistance if game shuts down
**/
case class Loot(val looter: String) extends Component {
	val typename = "loot-open"

	def isLootable(id: String): Boolean = {
		id == looter
	}

	def asJson(): JObject = {
		("loot" -> 
			("id" -> looter))
	}
}