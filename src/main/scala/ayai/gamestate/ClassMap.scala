package ayai.gamestate

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import ayai.factories.ClassFactory
import scala.collection.mutable.ArrayBuffer

case class AddClass(id: String, thisClass: ClassValues)
case class GetClass(id: String)
case class RemoveClass(id: String)
case class OutputJson()
case class ClassValues(
      id: Int,
      name: String,
      description: String,
      baseHealth: Int,
      baseMana: Int,
      baseStats: Stats)
class ClassMap() extends Actor {
	val classMap: HashMap[String, ClassValues] = HashMap[String, ClassValues]()

	def receive = {
		case AddClass(id: String, thisClass: ClassValues) =>
		  classMap(id) = thisClass
		case GetClass(id: String) =>
		  sender ! classMap(id)
		case RemoveClass(id: String) =>
		  classMap -= id
		case OutputJson() =>
			// classMap.foreach{case (key, value) => value.asJson}
		case _ => println("No Command for Classes")
	 		sender ! Failure
	}
}
