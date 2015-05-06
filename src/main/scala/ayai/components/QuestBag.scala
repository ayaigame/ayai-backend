package ayai.components

import crane.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

case class QuestBag(quests: ArrayBuffer[Quest] = new ArrayBuffer[Quest]()) extends Component {
  val typename = "quest-offer"
  def asJson(): JObject = {
    ("quests" -> quests.map{quest => quest.asJson})
  }

  def addQuest(questToAdd: Quest): Unit = {
    println("Quest with ID: " + questToAdd.id + " Added to Bag" )

    if (questToAdd != null) {
      quests += questToAdd
    }
  }
}