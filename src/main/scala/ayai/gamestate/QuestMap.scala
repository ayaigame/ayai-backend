package ayai.gamestate

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class AddQuest(id: String, quest: Quest)
case class RemoveQuest(id: String)
case class GetQuest(id: String)

class QuestMap() extends Actor {
	val questMap: HashMap[String, Quest] = HashMap[String, Quest]()

	def addQuest(id: String, quest: Quest) = {
		questMap(id) = quest
	}

	def removeQuest(id: String) = {
		questMap -= id
	}

	def getQuest(id: String) = {
		sender ! questMap(id)
	}

	def outputJson(): JObject = {
		// questMap.foreach{case (key, value) => value.asJson}
	}
	def receive = {
		case AddQuest(id: String, quest: Quest) => addQuest(id, quest)
		case RemoveQuest(id: String) => removeQuest(id)
		case GetQuest(id: String) => getQuest(id)
		case OutputJson() => outputJson
		case _ =>
			sender ! Failure
	}	
}