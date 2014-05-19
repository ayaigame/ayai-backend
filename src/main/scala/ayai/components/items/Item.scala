package ayai.components

/** Crane Imports **/
import crane.Component
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ayai.statuseffects._

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


class Item(val id: Long, name: String, value: Int, var weight: Double, 
           val itemType: ItemType, val effects: ArrayBuffer[Effect] = new ArrayBuffer[Effect]()) extends Component{
  var image: String = ""
  def asJson: JObject = {
    ("id" -> id) ~
  	("name" -> name) ~
    ("value" -> value) ~
    ("weight" -> weight) ~
    ("image" -> image) ~
    ("itemType" -> itemType.asJson)
  }

  def copy(): Item = {
  	val item = new Item(id, name, value, weight, itemType.copy)
    item.image = image
    item
  }
}
