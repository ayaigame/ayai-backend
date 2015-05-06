package ayai.quests

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class CharacterRoles(
              var fighter: Float = 0,
              var methodActor: Float = 0,
              var storyteller: Float = 0,
              var tactician: Float = 0,
              var powerGamer: Float = 0 ) {

  def asJson(): JObject = {
    ("fighter" -> fighter) ~
      ("methodActor" -> methodActor) ~
      ("storyteller" -> storyteller) ~
      ("tactician" -> tactician) ~
      ("powerGamer" -> powerGamer)
  }
}
