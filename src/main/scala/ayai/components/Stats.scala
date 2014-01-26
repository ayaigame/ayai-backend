package ayai.components

import com.artemis.Component

import scala.collection.mutable._

case class Stat(attributeType: String, magnitude: Int)

class Stats(stats: ArrayBuffer[Stat]) extends Component {
  def this() = this(new ArrayBuffer())

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
    //stats -= stats.find((stat: Stat) => stat.attributeType == statName)
  }

  // override def toString: String = {
    // "{\"name\": " + name + "}"
  // }

  
}