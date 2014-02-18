package ayai.components

/** Crane Imports **/
import crane.Component

/** External Imports **/
import scala.collection.mutable._

case class Stat(attributeType: String, magnitude: Int)

class Stats(stats: ArrayBuffer[Stat]) extends Component {
  def this() = this(new ArrayBuffer[Stat]())

  def addStat(newStat: Stat) = {
    stats += newStat
  }

  def addStat(attributeType: String, magnitude: Int) = {
    stats += Stat(attributeType, magnitude)
  }

  //Removes all stats which match the statName
  //hopefully should only ever be 1 match
  def removeStat(statName: String) = {
    stats.remove(stats.indexWhere((stat: Stat) => stat.attributeType == statName))
  }

  override def toString() = {
    "Stat: " + stats(0).attributeType + ", magnitude: " + stats(0).magnitude
  }
}
