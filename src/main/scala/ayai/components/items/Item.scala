package ayai.components

import com.artemis.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._


class Item(name: String, itemType: String, value: Int, weight: Double) extends Component{

  def asJson : JObject = {
  	("name" -> name) ~
    ("value" -> value) ~
    ("weight" -> weight) 
  }

  def getWeight : Double = {
  	weight
  }
}