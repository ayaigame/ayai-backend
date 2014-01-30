package ayai.components

import com.artemis.Component
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

case class Character(val id: String) extends Component{
	implicit val formats = Serialization.formats(NoTypeHints)

	implicit def asJson() : JObject = {
		("id" -> id)
	}

	override def toString: String = {
	  	write(this)
	}
}