package ayai.components

import com.artemis.Component

case class Weapon(
    name: String,
    value: Int,
    weight: Double,
    range: Int,
    damage: Int,
    damageType: String)
  extends Item(name, itemType = "weapon", value, weight) {

  override def toString: String = {
    "{\"name\": " + name + "}"
  }

  
}