package ayai.maps

/** External Imports **/
import scala.collection.mutable.ListBuffer
import ayai.components._
/**
** value is the sprite id that we need to load on the frontend
** layer are 
**/
case class Tile(val layers: ListBuffer[Layer], var tilePosition: Position = new Position(0,0), var indexPosition: Position = new Position(0,0)) {
  def isCollidable: Boolean = {
    for(layer <- layers) layer match {
      case CollidableLayer(_) => return true 
      case _ => {
        
      }
    }
    false
  }
}
