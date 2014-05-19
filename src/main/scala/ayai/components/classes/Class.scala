package ayai.components

import crane.Component
/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._

class Class(id: Int, name: String, baseHealth: Int, baseMana: Int) extends Component{
  override def toString: String = {
    "{\"name\": " + name + "}"
  }

  def asJson(): JObject = {
  	("class" ->
  		("id" -> id) ~
  		("name" -> name) ~
  		("baseHealth" -> baseHealth) ~
  		("baseMana" -> baseMana))
  }
}