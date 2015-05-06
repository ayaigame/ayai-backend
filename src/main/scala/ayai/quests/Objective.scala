package ayai.quests

import net.liftweb.json._

abstract class Objective (name: String) {
  implicit def formats = DefaultFormats + ShortTypeHints(List(classOf[FetchObjective], classOf[KillObjective]))

  def asJson: JObject
  def isComplete: Boolean
}