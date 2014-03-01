package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class AI(id: String, name: String, level: Int) extends Component {
	implicit val formats = Serialization.formats(NoTypeHints)

 	implicit def asJson() : JObject = {
    	("id" -> id) ~
    	("name" -> name) ~
    	("level" -> level)
  	}

  	override def toString: String = {
    	write(this)
  	}
}
