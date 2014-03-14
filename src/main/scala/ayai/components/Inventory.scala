package ayai.components

import crane.Component


/** External Imports **/
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}



case class Inventory (val inventory: ArrayBuffer[Item] = new ArrayBuffer[Item]()) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit def asJson: JObject = {
    ("inventory" -> inventory.map{ i =>
        (i.asJson)})
  }

  override def toString: String = {
    write(this)
  }

  def addItem(itemToAdd: Item) = {
    if(itemToAdd != null) {
      inventory += itemToAdd
    }
  }

  def removeItem(itemToRemove: Item) = {
    inventory -= itemToRemove
  }

  def hasItem(itemToCheck: Item) : Boolean = {
    inventory.contains(itemToCheck)
  }

  def getItem(itemLocation : Int) : Item = {
    inventory(itemLocation)
  }

  def totalWeight : Double = {
    inventory map (_.weight) reduceLeft (_+_)
  }

  def copy(): Inventory = {
    var copyInventory: ArrayBuffer[Item] = new ArrayBuffer[Item]()
    for(item <- inventory) {
      copyInventory += item.copy
    }
    new Inventory(copyInventory)
  }

}
