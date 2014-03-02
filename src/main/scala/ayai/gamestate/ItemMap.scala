package ayai.gamestate

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}


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

	def receive = {
		case AddItem(id: String, item: Item) => addItem(id, item)
		case GetItem(id: String) => getItem(id)
		case RemoveItem(id: String) => removeItem(id)
		case _ => println("No Command for Items")
			sender ! Failure
	}
}