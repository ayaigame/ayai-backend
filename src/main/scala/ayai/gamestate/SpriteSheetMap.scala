package ayai.gamestate

import akka.actor.Actor
import ayai.components._
import akka.actor.Status.Failure

case class AddSprite(id: String, sprite: SpriteSheet)
case class RemoveSprite(id: String)
case class GetSprite(id: String)

class SpriteSheetMap extends Actor {
  private val spriteMap = collection.mutable.HashMap[String, SpriteSheet]()

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
    case OutputJson() => outputJson()
    case _ => sender ! Failure
  }
}