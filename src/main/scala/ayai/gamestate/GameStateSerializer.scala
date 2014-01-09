package ayai.gamestate

/** Artemis Imports **/
import com.artemis.{Entity, World}
import com.artemis.managers.{TagManager, GroupManager}
import com.artemis.utils.{Bag, ImmutableBag}

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef}

/** Ayai Imports **/
import ayai.components.{Player, Position, Health, Room}

/** External Imports **/
import scala.collection.mutable.ArrayBuffer

sealed trait QueryType

case class PlayerRadius(characterId: String) extends QueryType
case class SomeData

class GameStateSerializer(world: World, loadRadius: Int) extends Actor {

  //Returns a list of entities contained within a room.
  def getRoomEntities(roomId: Int): ImmutableBag[Entity] = {
    world.getManager(classOf[GroupManager]).getEntities("ROOM" + roomId)
  }

  //Returns a character's belongings and surroundings.
  def getPlayerRadius(characterId: String) = {
    println("FETCHING PLAYER: " + characterId)
    val characterEntity : Entity = world.getManager(classOf[TagManager]).getEntity(characterId)
    val room = characterEntity.getComponent(classOf[Room])

    val otherEntities: ImmutableBag[Entity] = getRoomEntities(room.id)

    var json = "{\"you\": " + getEntityInfo(characterEntity) + ", \"others\": ["

    var entitiyJSONs = new ArrayBuffer[String]()
    for(i <- 0 until otherEntities.size()) {
      if(characterEntity.getId() != otherEntities.get(i).getId()) {
        entitiyJSONs += getEntityInfo(otherEntities.get(i))
      }
    }

    //Kinda need a do-while because the first one doesn't prepend a comma
    if(entitiyJSONs.size > 0 ) {
      json = json + entitiyJSONs(0)
    }

    for(i <- 1 until entitiyJSONs.size) {
      json = json + ", " + entitiyJSONs(i)
    }

    json = json + "]}"

    println(json)
  }

  //Once players actually have belonging we'll want to implement this and use it in getPlayerRadius
  // def getPlayerBelongings(playerId: String) = {

  // }

  //Returns the information about other entities within a room that a character might need to know
  def getEntityInfo(e: Entity): String = {
    val entityHealth = e.getComponent(classOf[Health])
    val entityPosition = e.getComponent(classOf[Position])
    var entityInfo = "{\"health\": " + entityHealth.toString
    return entityInfo + ", \"position\": " + entityPosition.toString + "}"
  }

  // def getSurroundings(pos: Position) = {

  // }

  def receive = {
    case PlayerRadius(characterId) => getPlayerRadius(characterId)
    case _ => println("Error: from serializer.")
  }
}
