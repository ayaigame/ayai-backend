package ayai.systems

import scala.collection.mutable.ListBuffer

import ayai.gamestate.AddQuest
import crane.{Entity, EntityProcessingSystem}

import java.rmi.server.UID

import scala.collection.mutable.ArrayBuffer
import scala.math

/** Akka Imports **/
import akka.actor.{ActorSystem, Props}
import ayai.components._
import ayai.quests._
import ayai.factories.EntityFactory

object QuestGenerationSystem {
  def apply(actorSystem: ActorSystem) = new QuestGenerationSystem(actorSystem)
}

class QuestGenerationSystem(actorSystem: ActorSystem) extends EntityProcessingSystem(include=List(classOf[GenerateQuest])) {
    def processEntity(e: Entity, deltaTime: Int): Unit = {
      e.getComponent(classOf[GenerateQuest]) match {
        case Some(genQuest: GenerateQuest) =>
          // fetch components we'll need to do something.
          val initiatorMemory = genQuest.initiator.getComponent(classOf[Memory]).asInstanceOf[Memory]
          val recipientHistory = genQuest.recipient.getComponent(classOf[QuestHistory])

          // create an objective list
          var objectives = new ListBuffer[Objective]()

          // next, we need to populate our quest with objectives. To do this, we'll find the most "significant" thing
          // in an NPC's memory.
          var significantMemory: MemoryContents = null
          for( memory : MemoryContents <- initiatorMemory.entitiesRemembered ) {
            if ( math.abs(memory.relationship) > math.abs(significantMemory.relationship) ) {
              significantMemory = memory
            }
          }

          // next, we can decide whether that thing is "negative" or "positive". If it's negative, the NPC will want that
          // thing to be killed or something. If it's positive, maybe you should bring it to them.
          /*if ( significantMemory != null ) {
            if ( significantMemory.relationship > 0 ) {
              // positive relationship quests
              objectives += new FetchObjective( "Thing To Get", "Bring it to this thing" )
            }else{
              // negative relationship quests.
              objectives += new KillObjective( "Get Entity Name from Passed ID Here", 0, 10 )
            }
          }*/

          // populate it with objectives
          objectives += new KillObjective("ENEMY ID HERE",10,0)

          // create a blank quest which we'll populate in a minute.
          val questComponent = new Quest(
            1,                    // id
            "hello",              // title
            "do a thing",         // description
            12,                   // recommended level
            objectives.toList     // objectives
          )

          // add a new quest to the quest bag of the initiator!
          genQuest.initiator.getComponent(classOf[QuestBag]).asInstanceOf[QuestBag].addQuest( questComponent )
          // the gamestate needs to keep a list of our quests?
          //networkSystem.actorSelection("user/QuestMap") ! AddQuest("QUEST"+questData.id, questComponent)

          // lastly, remove the generation request. We don't need it anymore.
          e.removeComponent( classOf[GenerateQuest] )
        case _ =>
      }
    }
}
