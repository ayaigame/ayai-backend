package ayai.gamestate

import akka.actor.Actor
import ayai.components._
import akka.actor.Status.Failure
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class AddItem(id: String, item: Item)
case class GetItem(id: String)
case class RemoveItem(id: String)

class ItemMap() extends Actor {

  // TODO make this thread-safe via java.util.concurrent.ConcurrentHashMap
	private val itemMap: collection.mutable.HashMap[String, Item] = collection.mutable.HashMap[String, Item]()

  def addItem(id: String, item: Item) = {
    itemMap(id) = item
  }

  def getItem(id: String): Unit = {
    try {
      val item = itemMap(id)
      sender ! item
    } catch {
      case _ : Throwable => sender ! new EmptySlot("")
    }
  }

  def removeItem(id: String): Unit = {
    itemMap -= id
  }

  def outputJson(): Unit = {
    val json = itemMap.mapValues(_.asJson)
    sender ! compact(render(json))
  }

  def receive = {
    case AddItem(id: String, item: Item) => addItem(id, item)
    case GetItem(id: String) => getItem(id)
    case RemoveItem(id: String) => removeItem(id)
    case OutputJson() => outputJson()
    case _ => println("No Command for Items")
      sender ! Failure
  }
}