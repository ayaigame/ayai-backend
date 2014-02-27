package ayai.components

import ayai.components._
import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


class Equipment() extends Component {
	object EmptySlot extends Item("",0,0,EmptyType) {}
	object EmptyType extends ItemType {
		override def asJson(): JObject = ("empty" -> "")
	}
	var helmet: Item = EmptySlot
	var weapon1: Item = EmptySlot
	var weapon2: Item = EmptySlot
	var torso: Item = EmptySlot
	var legs: Item = EmptySlot
	var feet: Item = EmptySlot

	def equipWeapon1(weapon: Item) {
		weapon.itemType match {
			case weaponType: Weapon => 
				weapon1 = weapon
			case _ =>
		}
	}
	def equipWeapon2(weapon: Item) {
		weapon.itemType match {
			case weaponType: Weapon => 
				weapon2 = weapon
			case _ =>
		}
	}

	def asJson(): JObject = {
		("equipment" ->
			("helmet" -> helmet.asJson) ~
			("weapon1" -> weapon1.asJson) ~
			("weapon2" -> weapon2.asJson) ~
			("torso" -> torso.asJson) ~
			("legs" -> legs.asJson) ~
			("feet" -> feet.asJson))
	}
}