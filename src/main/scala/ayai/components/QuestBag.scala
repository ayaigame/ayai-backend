package ayai.components

import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

class QuestBag(listOfQuests: ArrayBuffer[Quest]) extends Component {
	def asJson(): JObject = {
		("quests" -> listOfQuests.map{quest => quest.asJson})
	}
}