package ayai.components

/** Crane Imports **/
import crane.Component
import scala.collection.mutable.{ArrayBuffer, HashMap}
import ayai.statuseffects._

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


class Item(val id: Long, val name: String, value: Int, var weight: Double, 
           val itemType: ItemType, val effects: ArrayBuffer[Effect] = new ArrayBuffer[Effect]()) extends Component{
  var image: String = ""
  def asJson: JObject = {
  	("name" -> name) ~
    ("value" -> value) ~
    ("weight" -> weight) ~
    ("image" -> image) ~
    ("itemType" -> itemType.asJson)
  }

  def copy(): Item = {
  	new Item(id, name, value, weight, itemType.copy)
  }
}
