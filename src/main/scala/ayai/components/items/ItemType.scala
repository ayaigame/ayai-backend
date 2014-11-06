package ayai.components

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

trait ItemType {
  def asJson(): JObject
  def copy(): ItemType
}

case class Consumable() extends ItemType{
  def asJson(): JObject = {
    ("type" -> "consumable")
  }

  def copy() : ItemType = {
    new Consumable()
  }
}
