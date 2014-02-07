package ayai.components


import com.artemis.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.Serialization.{read, write}




class PlayerInventory(inventory : ArrayBuffer[Item], var equippedWeapon : Weapon, var equippedArmor: Armor) extends Inventory(inventory) { 

	override def asJson : JObject = {
		("inventory" -> inventory.map{ i =>
				(i.asJson)}) ~
		("equippedArmor" -> equippedArmor.asJson) ~
		("equippedWeapon" -> equippedWeapon.asJson)
	}



}