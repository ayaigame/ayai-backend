package ayai.components


import com.artemis.Component
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.Serialization.{read, write}



case class Inventory (inventory : ArrayBuffer[Item]) extends Component {
	implicit val formats = Serialization.formats(NoTypeHints)



	override def toString: String = {
		write(this)
	}

}