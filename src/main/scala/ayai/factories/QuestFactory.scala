package ayai.factories

import ayai.components._
import ayai.quests._
/** Crane Imports **/
import crane.{Entity, World}

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable._


class QuestFactory {
	case class AllQuestValues(
      id: Int,
      title: String,
      description: String,
      recommendLevel: String,
      objective: Objective)

  def bootup(world: World) = {
    val quests: List[Quest] = getQuestList("src/main/resources/configs/classes/Quests.json")

    quests.foreach (questData => {
      var entityQuest: Entity = world.createEntity(tag="QUEST"+questData.id)
      var questComponent = new Quest(
        questData.id,
        questData.title,
        questData.description,
        questData.recommendLevel,
        questData.objective)

      entityQuest.components += questComponent

      world.addEntity(entityQuest)
      // world.getManager(classOf[TagManager]).register("CLASSES" + classData.id, entityClass)
    })
  }


  def getQuestList(path: String): List[Quest] = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    val rootClasses = (parsedJson \\ "quests").extract[List[Quest]]

    // val listOfLists: List[List[Quest]] = rootClasses.map((path: String) => getClassesList(path))
    
    var questList = new ArrayBuffer[Quest]()
    questList.appendAll(rootClasses)
    questList.toList
    // listOfLists.foreach(e => questList.appendAll(e))
    // questList.toList
  }
}