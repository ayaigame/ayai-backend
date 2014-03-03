package ayai.components

import ayai.components._
import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class EmptySlot extends Item("",0,0,EmptyType) {}
object EmptyType extends ItemType {
    override def asJson(): JObject = ("empty" -> "")
    override def copy(): ItemType = EmptyType
  }
class Equipment() extends Component {

  var helmet: Item = new EmptySlot
  var weapon1: Item = new EmptySlot
  var weapon2: Item = new EmptySlot
  var torso: Item = new EmptySlot
  var legs: Item = new EmptySlot
  var feet: Item = new EmptySlot

  //if true then it is right weapon type and is equipped
  // and put old item back in
  //and take old back
  // return to sender a success
  def equipWeapon1(weapon: Item): Boolean = {
    weapon.itemType match {
      case weaponType: Weapon => 
        weapon1 = weapon
        true
      case _ =>
      false
    }
  }
  def equipWeapon2(weapon: Item): Boolean = {
    weapon.itemType match {
      case weaponType: Weapon => 
        weapon2 = weapon
        true
      case _ =>
      false
    }
  }
  def equipHelmet(helmet: Item): Boolean = {
    helmet.itemType match {
      case armorType: Armor => 
        this.helmet = helmet
        true
      case _ =>
      false
    }
  }
  def equipFeet(feet: Item): Boolean = {
    feet.itemType match {
      case armor: Armor => 
        this.feet = feet
        true
      case _ =>
      false
    }
  }
  def equipLegs(legs: Item): Boolean = {
    legs.itemType match {
      case armorType: Armor => 
        this.legs = legs
        true
      case _ =>
      false
    }
  }
  def equipTorso(torso: Item): Boolean = {
    torso.itemType match {
      case armorType: Armor => 
        this.torso = torso
        true
      case _ =>
      false
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