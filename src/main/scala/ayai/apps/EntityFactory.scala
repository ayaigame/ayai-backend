package ayai.apps

import com.artemis.Component
import com.artemis.Entity
import com.artemis.managers.{TagManager, GroupManager}
import com.artemis.World

import ayai.components._
import ayai.maps.Tile

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.io.Source

object EntityFactory {
  
  /**
   * might have to do some networking stuff, dont know yet
   */
   /**
  def createCharacter(world : World, roomId: Int, x: Int, y : Int) : Entity = {
    var character : Entity = world.createEntity()
    var position : Position = new Position(x,y)
    var velocity : Velocity = new Velocity(3,4)
    character.addComponent(position)
    
    character.addComponent(velocity)
    
    
    var health : Health = new Health(200,200);
    character.addComponent(health)
    
    character.addComponent(new Character(GameState.getNextCharacterId()));
    GameState.addCharacter(roomId, character)
    world.getManager(classOf[GroupManager]).add(character, Constants.PLAYER_CHARACTER)
    println("Entity: " + character.getId())
    GameLoop.map.addEntity(character.getId(),x,y)
    character;
  }

  def createItem(world : World, x : Int, y :Int, name : String) : Entity = {
    var item : Entity = world.createEntity()
    var position : Position = new Position(x, y)
    item.addComponent(position)

    var containable : Containable = new Containable(3, name, null)
    item.addComponent(containable)

    world.getManager(classOf[GroupManager]).add(item,"ITEM")
    
    GameLoop.map.addEntity(item.getId(),x,y)
    item
  }
	**/
  def createRoom(world : World, roomId : Int, tileMap : TileMap ) : Entity = {
  	var entityRoom : Entity = world.createEntity()
  	var room : Room = new Room(roomId)

  	entityRoom.addComponent(room)
  	entityRoom.addComponent(tileMap)

  	world.getManager(classOf[GroupManager]).add(entityRoom, "ROOMS")
  	//world.getManager(classOf[TagManager]).register(roomId.toString,entityRoom)

  	entityRoom
  }

  case class TMap(width : Int, height : Int)
  case class Tiles(data : List[Int], width : Int, height : Int)

  def loadRoomFromJson(world : World, roomId : Int, jsonFile : String) : Entity = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val file = Source.fromURL(getClass.getResource("/assets/maps/" + jsonFile))
    //supposedily implementation is slow so if we have large maps, may want to find another way of getting file
//    val source = Source.fromFile(jsonFile)
    val lines = file.mkString
    file.close()

    val parsedJson = parse(lines)
    val tmap = parsedJson.extract[TMap]
    val bundles = (parsedJson \\ "layers").extract[List[Tiles]]
    bundles.map{bundle => ("data" -> bundle.data, "height" -> bundle.height, "width" -> bundle.width)}
//           .foreach{j => println(compact(render(j)))}
    val arrayTile : Array[Array[Tile]] = Array.fill[Tile](tmap.width, tmap.height)(new Tile())
    val height : Int = tmap.height
    val width : Int = tmap.width
     println("Need to test1: " + bundles(0).data(0))
    for(i <- 0 until (width*height)) {
        arrayTile(i/width)(i%height) = new Tile(bundles(0).data(i))
      }
    val tileMap : TileMap = new TileMap(arrayTile)
    tileMap.height = tmap.height
    tileMap.width = tmap.width
    tileMap.file = jsonFile
    //create tilemap
    val entityRoom : Entity = createRoom(world, roomId, tileMap)
    //println(lines)
    entityRoom
  }
}
