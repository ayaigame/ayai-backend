package ayai.components

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class Armor(
  name: String,
  value: Int,
  weight: Double,
  slot: String,
  protection: Int)
  extends Item(name, itemType = "armor", value, weight) {
    override def asJson : JObject = {
     ("name" -> name) ~
     ("value" -> value) ~
     ("weight" -> weight) ~
     ("slot" -> slot) ~
     ("protection" -> protection) 
    }
  
}
