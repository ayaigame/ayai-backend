package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


class Item(name: String, value: Int, var weight: Double, val itemType: ItemType) extends Component{
  var image: String = ""
  def asJson: JObject = {
  	("name" -> name) ~
    ("value" -> value) ~
    ("weight" -> weight) ~
    ("image" -> image) ~
    ("itemType" -> itemType.asJson)
  }
}
