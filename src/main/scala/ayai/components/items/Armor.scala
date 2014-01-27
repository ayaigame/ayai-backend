package ayai.components

import com.artemis.Component

case class Armor(
    name: String,
    value: Int,
    weight: Double,
    slot: String,
    protection: Int)
  extends Item(name, itemType = "armor", value, weight) {
  
  // override def toString: String = {
  //   "{\"name\": " + name + "}"
  // }

  
}