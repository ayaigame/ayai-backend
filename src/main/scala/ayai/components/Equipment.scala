package ayai.components

import ayai.components._
import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import scala.collection.mutable.HashMap

case class EmptySlot extends Item(0,"",0,0,EmptyType) {}
object EmptyType extends ItemType {
    override def asJson(): JObject = ("empty" -> "")
    override def copy(): ItemType = EmptyType
  }
class Equipment() extends Component {
  val equipmentMap: HashMap[String,Item] = new HashMap[String, Item]()
  equipmentMap += ("helmet" -> new EmptySlot())
  equipmentMap += ("weapon1" -> new EmptySlot())
  equipmentMap += ("weapon2" -> new EmptySlot())
  equipmentMap += ("torso" -> new EmptySlot())
  equipmentMap += ("feet" -> new EmptySlot())
  equipmentMap += ("legs" -> new EmptySlot())
  //if true then it is right weapon type and is equipped
  // and put old item back in
  //and take old back
  // return to sender a success
  def equipItem(item: Item): Boolean = {
    item.itemType match {
      case weapon: Weapon =>
        weapon.itemType match {
          case "weapon1" => 
            equipmentMap(weapon.itemType) = item
            return true
          case "weapon2" => 
            equipmentMap(weapon.itemType) = item
            return true
          case _ =>
            return false
        }
      case armor: Armor =>
        armor.itemType match {
          case "helmet" => 
            equipmentMap(armor.itemType) = item
            return true
          case "feet" =>
            equipmentMap(armor.itemType) = item
            return true
          case "torso" =>
            equipmentMap(armor.itemType) = item
            return true
          case "legs" =>
            equipmentMap(armor.itemType) = item
            return true
          case _ =>
            return false
        }
      case _ => 
        return false
    }
  }

  def unequipItem(equipmentType: String): Item = {
    val equippedItem = equipmentMap(equipmentType).copy
    equipmentMap(equipmentType) = new EmptySlot
    return equippedItem

  }


  def asJson(): JObject = {
    ("equipment" ->
      ("helmet" -> equipmentMap("helmet").asJson) ~
      ("weapon1" -> equipmentMap("weapon1").asJson) ~
      ("weapon2" -> equipmentMap("weapon2").asJson) ~
      ("torso" -> equipmentMap("torso").asJson) ~
      ("legs" -> equipmentMap("legs").asJson) ~
      ("feet" -> equipmentMap("feet").asJson))
  }
}