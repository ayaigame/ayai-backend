package ayai.components

/** Ayai Imports **/
import ayai.gamestate.Effect

/** Crane Imports **/
import crane.Component

/** External Imports **/
import scala.collection.mutable.ArrayBuffer
/*
* This is the item class essentially, will tell whether an item 
* can be held by a character
*/
class Containable(var id : Int, var name : String, var effects : ArrayBuffer[Effect] ) extends Component {
  
  def addEffect(effect : Effect) {
    effects += effect
  }

}
