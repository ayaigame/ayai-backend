package ayai.actions

/** Crane Imports **/
import crane.Entity

/** External Imports **/
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

trait Action { 
 def process(e : Entity)
 def asJson(): JObject 
}

case class ItemAction(var itemAction: ItemAct) extends Action {
  def process(e: Entity) {
    // TODO get the inventory of the character and base it off what action of the command given
  }

  def asJson(): JObject = "action" -> "empty"
}

case class AttackAction(var action: AttackAct) extends Action {
  def process(e: Entity) {
    // TODO print something
  }

  def asJson(): JObject = "action" -> "empty"
}
