package ayai.gamestate

import akka.actor.Actor
import akka.actor.Status.Failure
import ayai.components._

case class AddQuest(id: String, quest: Quest)
case class RemoveQuest(id: String)
case class GetQuest(id: String)

class QuestMap() extends Actor {
  val questMap = collection.mutable.HashMap[String, Quest]()

  def addQuest(id: String, quest: Quest) = {
    questMap(id) = quest
  }

  def removeQuest(id: String) = {
    questMap -= id
  }

  def getQuest(id: String) = {
    sender ! questMap(id)
  }

  def outputJson() {
    "quests" -> questMap.mapValues(_.asJson())
  }

  def receive = {
    case AddQuest(id: String, quest: Quest) => addQuest(id, quest)
    case RemoveQuest(id: String) => removeQuest(id)
    case GetQuest(id: String) => getQuest(id)
    case OutputJson() => outputJson()
    case _ => sender ! Failure
  }
}