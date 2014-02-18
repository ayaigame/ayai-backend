package ayai.factories

import ayai.components._

/** Crane Imports **/
import crane.{Entity, World}

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable._

object ClassFactory {

  case class AllClassValues(
      id: Int,
      name: String,
      baseHealth: Int,
      baseMana: Int,
      baseStats: Option[List[Stat]],
      statGrowths: Option[List[Stat]])

  def bootup(world: World) = {
    val classes: List[AllClassValues] = getClassesList("src/main/resources/configs/classes/classes.json")

    classes.foreach (classData => {
      var entityClass: Entity = world.createEntity(tag="CLASS"+classData.id)
      var classComponent = new Class(
        classData.id,
        classData.name,
        classData.baseHealth,
        classData.baseMana)

      entityClass.components += classComponent

      //Construct stats component
      entityClass.components += buildStats(classData.baseStats)

      world.addEntity(entityClass)
      // world.getManager(classOf[TagManager]).register("CLASSES" + classData.id, entityClass)
    })
  }

  def buildStats(stats: Option[List[Stat]]): Stats = {
    var statsArray = new ArrayBuffer[Stat]()
    statsArray.appendAll(stats.get)
    new Stats(statsArray)
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
