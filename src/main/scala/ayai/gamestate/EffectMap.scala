package ayai.gamestate


import ayai.statuseffects._
import akka.actor.Actor
import scala.collection.mutable.HashMap
import akka.actor.Status.Failure
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class AddEffect(id: String, effect: Effect)
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

  def outputJson() = {
    sender ! compact(render(effectMap.mapValues(_.asJson)))
  }

  def receive = {
    case AddEffect(id: String, effect: Effect) => addEffect(id, effect)
    case RemoveEffect(id: String) => removeEffect(id)
    case GetEffect(id: String) => getEffect(id)
    case OutputJson() => outputJson()
    case _ => sender ! Failure
  }
}