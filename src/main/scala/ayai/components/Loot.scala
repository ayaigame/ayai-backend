package ayai.components

import crane.{Component, Entity}


/**
** looter: String - the account id of the person who can see it/loot the inventory
** I make it account id based in case we want persistance if game shuts down
**/
case class Loot(val looter: String) {
	def isLootable(id: String): Boolean = {
		id == looter
	}
}