package ayai.components

import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

class QuestBag(quests: ArrayBuffer[Quest] = new ArrayBuffer[Quest]()) extends Component {
  def asJson(): JObject = {
    ("quests" -> quests.map{quest => quest.asJson})
  }

  def addQuest(questToAdd: Quest) = {
      if(questToAdd != null) {
        quests += questToAdd
        }
  
    }
}