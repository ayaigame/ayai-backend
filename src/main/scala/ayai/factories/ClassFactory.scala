package ayai.factories

import ayai.components._
import ayai.gamestate._

/** Crane Imports **/
import crane.{Entity, World}

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Extraction._
// import net.liftweb.json.Printer._

import scala.collection.mutable._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case class InvalidJsonException(smth:String)  extends Exception

object ClassFactory {
  implicit val formats = net.liftweb.json.DefaultFormats
  val classes: List[AllClassValues] = getClassesList("src/main/resources/configs/classes/classes.json")

  case class AllClassValues(
      id: Int,
      name: String,
      description: String,
      baseHealth: Int,
      baseMana: Int,
      baseStats: Option[List[Stat]],
      statGrowths: Option[List[Stat]]) {

    def getStatsJson: JValue = {
      baseStats match {
        case Some(stats: List[Stat]) =>
          // var jsonStats =
          var statsArray = new ArrayBuffer[Stat]()
          statsArray appendAll stats
          statsArray += new Stat("health", baseHealth)
          statsArray += new Stat("mana", baseMana)

          var statsMapping = statsArray map ((stat: Stat) => (stat.attributeType -> stat.magnitude))
          var statsMap = statsMapping.toMap
          statsMap

        case _ =>
          throw new InvalidJsonException("Cannot read stats in class factory.")
      }
    }

    def asJson: JObject = {
      ("id" -> id) ~
        ("name" -> name) ~
        ("description" -> description) ~
        ("stats" -> getStatsJson)
    }
  }

  def bootup(networkSystem: ActorSystem) = {
    classes.foreach(classData => {
      var classComponent = new Class(
        classData.id,
        classData.name,
        classData.baseHealth,
        classData.baseMana)

      //Construct stats component
      // classComponent.components += buildStats(classData.baseStats)

      networkSystem.actorSelection("user/ClassMap") ! AddClass("CLASS"+classData.id, classComponent)
    })
  }

  def buildStats(stats: Option[List[Stat]]): Stats = {
    var statsArray = new ArrayBuffer[Stat]()
    statsArray.appendAll(stats.get)
    new Stats(statsArray)
  }

  def asJson: JObject = {
    ("classes" -> classes.map{ c =>
        (c.asJson)})
  }

  def getClassesList(path: String): List[AllClassValues] = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    val rootClasses = (parsedJson \\ "classes").extract[List[AllClassValues]]
    val otherPaths = (parsedJson \\ "externalClasses").extract[List[String]]

    val listOfLists: List[List[AllClassValues]] = otherPaths.map((path: String) => getClassesList(path))

    var classesList = new ArrayBuffer[AllClassValues]()
    classesList.appendAll(rootClasses)

    listOfLists.foreach(e => classesList.appendAll(e))
    classesList.toList
  }
}
