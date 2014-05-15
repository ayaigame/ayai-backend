package ayai.gamestate

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.collection.mutable.HashMap
import ayai.components._
import akka.actor.Status.{Success, Failure}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class AddSprite(id: String, sprite: SpriteSheet)
case class RemoveSprite(id: String)
case class GetSprite(id: String)

class SpriteSheetMap() extends Actor {
	val spriteMap: HashMap[String, SpriteSheet] = HashMap[String, SpriteSheet]()

	def addSprite(id: String, sprite: SpriteSheet) = {
		spriteMap(id) = sprite
	}

	def removeSprite(id: String) = {
		spriteMap -= id
	}

	def getSprite(id: String) = {
		sender ! spriteMap(id)
	}

	def outputJson() {
		// questMap.foreach{case (key, value) => value.asJson}
	}
	def receive = {
		case AddSprite(id: String, sprite: SpriteSheet) => addSprite(id, sprite)
		case RemoveSprite(id: String) => removeSprite(id)
		case GetSprite(id: String) => getSprite(id)
		case OutputJson() => outputJson
		case _ =>
			sender ! Failure
	}	
}