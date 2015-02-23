package ayai.components

/** Crane Imports **/
import crane.Component

import scala.collection.mutable.ArrayBuffer

/** External Imports **/
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

class SenseComponent extends Component {
  def notifySystem() = {
    //...
  }
}

class Hearing extends SenseComponent {
  var hearingAbility : Int = -1
}

class SoundProducing extends Component {
  var intensity : Int = -1
}

class Vision extends SenseComponent {
  var visionRange : Int = -1
  def drawLine(start: Position, end : Position): Boolean = {
    //...
    false
  }
}

class BresenhamLOS extends Vision {
  override def drawLine(start: Position, end : Position): Boolean = {
    //...
    false
  }
}

class WuLOS extends Vision {
  override def drawLine(start: Position, end : Position): Boolean = {
    //...
    false
  }
}

class MemoryContents {
  var entityID: Int = -1
  var entityPosition: Position = null
  var relationship: Int = -1
}

class Memory extends SenseComponent {
  var memoryAbility: Int = -1
  var entitiesRemembered: ArrayBuffer[MemoryContents] = new ArrayBuffer[MemoryContents]()
}