package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


case class Weapon(
    range: Int,
    damage: Int,
    damageType: String,
    itemType: String)
  extends ItemType {

  override def asJson(): JObject = {
      ("range" -> range) ~
      ("damage" -> damage) ~
      ("damageType" -> damageType) ~
      ("type" -> itemType)
  }
  override def copy(): ItemType = {
    new Weapon(range, damage, damageType, itemType)
  }
}
