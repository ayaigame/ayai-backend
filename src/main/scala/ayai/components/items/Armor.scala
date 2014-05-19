package ayai.components

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class Armor(
  slot: String,
  protection: Int,
  itemType: String)
  extends ItemType {

    def asJson(): JObject = {
      ("slot" -> slot) ~
      ("protection" -> protection) ~
      ("type" -> "armor")
    }
	override def copy(): ItemType = {
    	new Armor(slot, protection, itemType)
  	}

}
