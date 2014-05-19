package ayai.gamestate

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._


case class AddItem(id: String, item: Item)
case class GetItem(id: String)
case class RemoveItem(id: String)

class ItemMap() extends Actor {
	val itemMap: HashMap[String, Item] = HashMap[String, Item]()

	def addItem(id: String, item: Item) = {
		itemMap(id) = item
	}

	def getItem(id: String) = {
		sender ! itemMap(id)
	}

	def removeItem(id: String) = {
		itemMap -= id
	}

	def outputJson() = {
		val json = (itemMap.map{case (key, value) => value.asJson})
		sender ! compact(render(json))
	}

	def receive = {
		case AddItem(id: String, item: Item) => addItem(id, item)
		case GetItem(id: String) => getItem(id)
		case RemoveItem(id: String) => removeItem(id)
		case OutputJson() => outputJson
		case _ => println("No Command for Items")
			sender ! Failure
	}
}