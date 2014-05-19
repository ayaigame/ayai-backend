package ayai.components

import ayai.components._
import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import scala.collection.mutable.HashMap

case class EmptySlot(slot:String) extends Item(-1,"",0,0,EmptyType(slot)) {}
case class EmptyType(slot: String) extends ItemType {
    override def asJson(): JObject = ("empty" -> "slot")
    override def copy(): ItemType = new EmptyType(slot)
  }
class Equipment() extends Component {
  val equipmentMap: HashMap[String,Item] = new HashMap[String, Item]()
  equipmentMap += ("helmet" -> new EmptySlot("helmet"))
  equipmentMap += ("weapon1" -> new EmptySlot("weapon1"))
  equipmentMap += ("weapon2" -> new EmptySlot("weapon2"))
  equipmentMap += ("torso" -> new EmptySlot("torso"))
  equipmentMap += ("feet" -> new EmptySlot("feet"))
  equipmentMap += ("legs" -> new EmptySlot("legs"))

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

  def equipItem(item: Item, slot: String): Boolean = {
    equipmentMap(slot) = item
    return true
  }

  def unequipItem(equipmentType: String): Item = {
    // if equipment map has nothing then return emptyslot
    var equippedItem: Item = null
    try {
      equippedItem = equipmentMap(equipmentType).copy
    } catch {
      case _ => equippedItem = new EmptySlot(equipmentType)
    }
    equipmentMap(equipmentType) = new EmptySlot(equipmentType)
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