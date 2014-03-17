package ayai.gamestate

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}


case class AddClass(id: String, theClass: Class)
case class GetClass(id: String)
case class RemoveClass(id: String)

class ClassMap() extends Actor {
	val classMap: HashMap[String, Class] = HashMap[String, Class]()

	def receive = {
		case AddClass(id: String, item: Class) =>
		  classMap(id) = item
		case GetClass(id: String) =>
		  sender ! classMap(id)
		case RemoveClass(id: String) =>
		  classMap -= id
		case _ => println("No Command for Classes")
	 		sender ! Failure
	}
}
