package ayai.apps

import com.artemis.Component
import com.artemis.Entity
import com.artemis.managers.{TagManager, GroupManager}
import com.artemis.World

import ayai.components._
import ayai.maps._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable.ListBuffer
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


  case class JTMap(id : Int, width : Int, height : Int)
  case class JTiles(data : List[Int], width : Int, height : Int, name : String)
  case class JTransport(start_x : Int, start_y : Int, end_x: Int, end_y : Int, toRoomFile : String, toRoomId : Int) {
    override def toString() : String = {
      return "start_x: " + start_x + " toRoomFile: " + toRoomFile
    }
  }
  case class JTilesets(image : String)
  
  def loadRoomFromJson(world : World, roomId : Int, jsonFile : String) : Entity = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val file = Source.fromURL(getClass.getResource("/assets/maps/" + jsonFile))
    //supposedily implementation is slow so if we have large maps, may want to find another way of getting file
//    val source = Source.fromFile(jsonFile)
    val lines = file.mkString
    file.close()

    val parsedJson = parse(lines)
    val tmap = parsedJson.extract[JTMap]
    val jtilesets = (parsedJson \\ "tilesets").extract[List[JTilesets]]
    val jtransports = (parsedJson \\ "transports").extract[List[JTransport]]
    var transports : List[TransportInfo] = Nil

    for(trans <- jtransports) {
      val startPosition = new Position(trans.start_x, trans.start_y)
      val endPosition = new Position(trans.end_x, trans.end_y)
      transports = new TransportInfo(startPosition, endPosition, trans.toRoomFile, trans.toRoomId) :: transports
    }
    val bundles = (parsedJson \\ "layers").extract[List[JTiles]]
    bundles.map{bundle => ("data" -> bundle.data, "height" -> bundle.height, "width" -> bundle.width, "name" -> bundle.name)}
//           .foreach{j => println(compact(render(j)))}
    val arrayTile : Array[Array[Tile]] = Array.fill[Tile](tmap.width, tmap.height)(new Tile(ListBuffer()))
    //get the overall size and id of maps
    val id : Int = tmap.id
    val height : Int = tmap.height
    val width : Int = tmap.width
    //get and transorm tiles from a list to multi-dimensional array
    for(i <- 0 until (width*height)) {
      for(bundle <- bundles) {
        if(bundle.data(i) != 0 ) {
          if(bundle.name != "collision") 
            arrayTile(i%width)(i/height).layers += new NormalLayer(bundle.data(i))
          else {
            arrayTile(i%width)(i/height).layers += new CollidableLayer(bundle.data(i))
            println("Height: " + (i%width) + " Row: " + (i/height) + " Value: " + bundle.data(i))
          }
        }
      }
    }
    //parse the tilesets and put them in a List[String]
    //by jarrad : THIS IS HORRID, REALLY NEED TO FIX THIS UP AND GET FEATURES WORKING FOR MAPS
    val images : ListBuffer[String] = ListBuffer()
    for(tileset <- jtilesets) {
      images += tileset.image
    }
    val tilesets = new Tilesets(images.toList)

    val tileMap : TileMap = new TileMap(arrayTile, transports, tilesets)
    tileMap.height = tmap.height
    tileMap.width = tmap.width
    tileMap.file = jsonFile
    //create tilemap
    val entityRoom : Entity = createRoom(world, id, tileMap)
    //println(lines)
    entityRoom
  }
}
