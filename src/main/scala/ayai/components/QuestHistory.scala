package ayai.components

import crane.Component
import ayai.quests.CharacterRoles
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

import scala.collection.mutable.ArrayBuffer

class QuestHistory(
            var completedQuests: ArrayBuffer[Quest] = new ArrayBuffer[Quest](),
            var preferences: CharacterRoles = new CharacterRoles() ) extends Component {

  def asJson(): JObject = {
      ("completedQuests" -> completedQuests.map{obj => obj.asJson}) ~
      ("preferences" -> preferences.asJson)
  }
}