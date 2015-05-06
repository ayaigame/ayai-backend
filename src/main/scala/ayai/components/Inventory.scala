package ayai.components

import crane.Component

/** External Imports **/
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

case class Inventory (inventory: ArrayBuffer[Item] = new ArrayBuffer[Item]()) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit def asJson: JObject = {
    "inventory" -> inventory.map{ i => (i.asJson) }
  }

  override def toString: String = {
    write(this)
  }

  def addItem(itemToAdd: Item): Unit = {
    if (itemToAdd != null) {
      inventory += itemToAdd
    }
  }

  def removeItem(itemToRemove: Item): Unit = {
    inventory -= itemToRemove
  }

  def hasItem(itemToCheck: Item): Boolean = {
    inventory.contains(itemToCheck)
  }

  def getItem(itemLocation : Int): Item = {
    inventory(itemLocation)
  }

  def getItemById(itemId: Int): Item = {
    for (item <- inventory) {
      if (item.id == itemId) {
        return item
      }
    }

    // TODO make this return an option
    null
  }

  def totalWeight: Double = {
    inventory.map(_.weight).sum
  }

  def copy(): Inventory = {
    new Inventory(inventory.map(_.copy()))
  }
}
