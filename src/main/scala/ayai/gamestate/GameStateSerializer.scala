package ayai.gamestate

/** Artemis Imports **/
import com.artemis.{Entity, World}
import com.artemis.managers.{TagManager, GroupManager}

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef}

/** Ayai Imports **/
import ayai.components.{Player, Position}

sealed trait QueryType

case class PlayerRadius(playerId: String) extends QueryType
case class SomeData

class GameStateSerializer(world: World, loadRadius: Int) extends Actor {
  val tagManager = world.getManager(classOf[TagManager])

  //Returns a list of entities contained within a room.
  // def getRoomEntities(roomId: Int): List[Entity] = {
    
  // }

  //Returns the data that a player needs to know when loading
  // def getState(requestType: QueryType): String = {
  //   requestType match {
  //     case PlayerRadius(playerId: Int) => {
  def getPlayerRadius(playerId: String) = {
    println("FETCHING PLAYER: " + playerId)
    val tempEntity : Entity = tagManager.getEntity(playerId)
    val player: Player =  tempEntity.getComponent(classOf[Player])
    val playerPos: Position = tempEntity.getComponent(classOf[Position])
    println("PLAYER " + playerId + " IS AT " + playerPos.x + ", " + playerPos.y)
  }
  //   }
  // }

  // def getState(requestType: QueryType): SomeData = {

  // }

  def receive = {
    case PlayerRadius(playerId) => getPlayerRadius(playerId)
    case _ => println("Error: from serializer.")
  }
}
