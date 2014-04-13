package ayai.components

/** Crane Imports **/
import crane.Component
import ayai.statuseffects._
/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

// Added tempValues so we can remove any effects from a  given stat
case class Stat(attributeType: String, magnitude: Double) {
  def asJson: JObject = {
    (attributeType -> magnitude)
  }

  /*
    Will first check if to process the effect again, and if invalid then remove the effect
  */
  def checkEffects() {

  }
}

class Stats(val stats: ArrayBuffer[Stat]) extends Component {

  def addStat(newStat: Stat) = {
    stats += newStat
  }

  def addStat(attributeType: String, magnitude: Double) = {
    stats += Stat(attributeType, magnitude)
  }

  //Removes all stats which match the statName
  //hopefully should only ever be 1 match
  def removeStat(statName: String) = {
    stats.remove(stats.indexWhere((stat: Stat) => stat.attributeType == statName))
  }

  def getAttributeByType(attributeType: String): Stat = {
    for(stat <- stats) {
      if(stat.attributeType == attributeType) {
        return stat
      }
    }
    return new Stat("", 0)
  }

  override def toString = {
      "STAT"
//    "Stat: " + stats(0).attributeType + ", magnitude: " + stats(0).magnitude
  }
}
