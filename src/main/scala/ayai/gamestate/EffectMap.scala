package ayai.gamestate

import crane._
import ayai.statuseffects._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}

case class AddEffect(id: String, Effect: Effect)
case class RemoveEffect(id: String)
case class GetEffect(id: String)


class EffectMap() extends Actor {
	val effectMap: HashMap[String, Effect] = HashMap[String, Effect]()

	def addEffect(id: String, effect: Effect) = {
		effectMap(id) = effect
	}

	def removeEffect(id: String) = {
		effectMap -= id
	}

	def getEffect(id: String) = {
		sender ! effectMap(id)
	}

	def receive = {
		case AddEffect(id: String, effect: Effect) => addEffect(id, effect)
		case RemoveEffect(id: String) => removeEffect(id)
		case GetEffect(id: String) => getEffect(id)
		case _ =>
			sender ! Failure
	}	
}