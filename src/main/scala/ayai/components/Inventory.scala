package ayai.components

import crane.Component


/** External Imports **/
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}



case class Inventory (inventory : ArrayBuffer[Item]) extends Component {
  implicit val formats = Serialization.formats(NoTypeHints)

  implicit def asJson : JObject = {
    ("inventory" -> inventory.map{ i =>
        (i.asJson)})
  }


  override def toString: String = {
    write(this)
  }


  def addItem(itemToAdd: Item) = {
    inventory += itemToAdd
  
  }

  def removeItem(itemToRemove: Item) = {
    inventory -= itemToRemove
  }


  def hasItem(itemToCheck: Item) : Boolean = {
    inventory.contains(itemToCheck)
  }

  def totalWeight() : Double = {
    var totalWeight = 0.0;
    inventory.foreach(e => totalWeight = totalWeight + e.getWeight)
    return totalWeight
  }

}