package ayai.factories

import ayai.components._
import ayai.gamestate._
import ayai.quests._

/** Crane Imports **/
import net.liftweb.json._

import akka.actor.ActorSystem

import scala.collection.mutable.ListBuffer


object QuestFactory {
  case class AllQuestValues(
      id: Int,
      title: String,
      description: String,
      recommendLevel: String,
      objective: Objective)

  def bootup(networkSystem: ActorSystem) = {
    val quests: List[Quest] = getQuestList("src/main/resources/quests/quests.json")

    quests.foreach (questData => {
      val questComponent = new Quest(
        questData.id,
        questData.title,
        questData.description,
        questData.recommendLevel,
        questData.objectives)

      networkSystem.actorSelection("user/QuestMap") ! AddQuest("QUEST"+questData.id, questComponent)
    })
  }

  def getQuestList(path: String): List[Quest] = {
    implicit val formats = DefaultFormats + ShortTypeHints(List(classOf[FetchObjective], classOf[KillObjective]))

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    // this is a pretty gross workaround, but for now it seems to be ok.
    var quests = new ListBuffer[Quest]()

    for ( questData <- (parsedJson \ "quests").children ) {

      var objectives = new ListBuffer[Objective]()
      // let's extract our objectives here too.
      for ( objectiveData <- (questData \ "objectives").children ) {
        // use type hinting (specified in format above) here to extract the objectives based on their class.
        // Note that this can probably be simplified... maybe even to one line, but right now I'm doing this.
        objectives += objectiveData.extract[Objective]
      }

      val quest = new Quest(
        (questData \ "id").extract[String].toInt,
        (questData \ "title").extract[String],
        (questData \ "description").extract[String],
        (questData \ "recommendLevel").extract[String].toInt,
        objectives.toList// (questData \ "objectives").extract[List[Objective]]
      )

      quests += quest
    }

    quests.toList

    //val rootClasses = (parsedJson \\ "quests").extract[List[Quest]]

    // val listOfLists: List[List[Quest]] = rootClasses.map((path: String) => getClassesList(path))

    //rootClasses

    // listOfLists.foreach(e => questList.appendAll(e))
    // questList.toList
  }
}