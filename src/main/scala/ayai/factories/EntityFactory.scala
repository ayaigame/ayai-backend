package ayai.factories

/** Ayai Imports **/
import ayai.components._
import ayai.maps._
import ayai.actions._
import ayai.persistence.AyaiDB
import ayai.apps.Constants

/** Crane Imports **/
import crane.Component
import crane.Entity
import crane.World


/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import org.mashupbots.socko.events.WebSocketFrameEvent
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import scala.io.Source

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef, Props, ActorSelection}


object EntityFactory {

  //Should take characterId: Long as a parameter instead of characterName
  //However can't do that until front end actually gives me the characterId
  def loadCharacter(world : World, webSocket: WebSocketFrameEvent, entityId: String, characterName: String, x: Int, y: Int, actor : ActorSelection) = {
    val p: Entity = world.createEntity(tag="CHARACTER"+entityId)
    val characterRow = AyaiDB.getCharacter(characterName)

    p.components += new Position(characterRow.pos_x,characterRow.pos_y)
    p.components += new Velocity(3,4)
    p.components += new Bounds(32, 32)
    p.components += new Velocity(4, 4)
    p.components += new Actionable(false, DownDirection)
    p.components += new Health(100,100)
    p.components += new NetworkingActor(actor)
    p.components += new Mana(200,200)
    p.components += new Room(characterRow.room_id)
    p.components += new Character(entityId, characterRow.name, 0, 1) //Should calculate level here

    val questbag = new QuestBag()
    questbag.addQuest(world.getEntityByTag("QUEST1") match {
      case Some(e: Entity) => e.getComponent(classOf[Quest]) match {
        case Some(quest: Quest) => quest 
        case _ => null
      }
      case _ => null
    })
    questbag.addQuest(world.getEntityByTag("QUEST2") match {
      case Some(e: Entity) => e.getComponent(classOf[Quest]) match {
        case Some(quest: Quest) => quest 
        case _ => null
      }
      case _ => null
    })

    p.components += questbag

    //Should add calculate and add stats
    val inventory = new Inventory()
    inventory.addItem(world.getEntityByTag("ITEMS0") match {
        case Some(e : Entity) => e.getComponent(classOf[Item]) match {
          case Some(it : Item) => it
          case _ => null
        }

        case _ => null
    })
    inventory.addItem(world.getEntityByTag("ITEMS1") match {
        case Some(e : Entity) => e.getComponent(classOf[Item]) match {
          case Some(it : Item) => it
          case _ => null
        }

        case _ => null
    })
    inventory.addItem(world.getEntityByTag("ITEMS2") match {
        case Some(e : Entity) => e.getComponent(classOf[Item]) match {
          case Some(it : Item) => it
          case _ => null
        }

        case _ => null
    })
    inventory.addItem(world.getEntityByTag("ITEMS1") match {
        case Some(e : Entity) => e.getComponent(classOf[Item]) match {
          case Some(it : Item) => it
          case _ => null
        }

        case _ => null
    })
    p.components += inventory

    val equipment = new Equipment()
    equipment.equipWeapon1(inventory.getItem(1))
    p.components += equipment

    world.addEntity(p)
    world.groups("CHARACTERS") += p
    world.groups("ROOM"+characterRow.room_id) += p

    //I think that there should probably be a lookup based on the character's room here.
    val roomInfo = world.getEntityByTag("ROOM"+characterRow.room_id) match {
      case Some(e: Entity) =>
        e
      case _ => //load default here (too bored to implement now) 
        null
    }

    val tilemap = roomInfo.getComponent(classOf[TileMap]) match {
      case Some(tileMap: TileMap) => tileMap
      case _ => null 
    }
    
    
    val json = (
      ("type" -> "id") ~
      ("id" -> entityId) ~
      ("x" -> x) ~
      ("y" -> y) ~
      ("tilemap" -> tilemap.file) ~
      (tilemap.tilesets.asJson)
    )

    webSocket.writeText(compact(render(json)))
  }

/**
  def createItem(world : World, x : Int, y :Int, name : String) : Entity = {
  var item : Entity = world.createEntity()
  var position : Position = new Position(x, y)
  item.components += (position)

  var containable : Containable = new Containable(3, name, null)
  item.components += (containable)

  world.getManager(classOf[GroupManager]).add(item,"ITEM")

  GameLoop.map.addEntity(item.getId(),x,y)
  item
  }
  **/
  def createRoom(world : World, roomId : Int, tileMap : TileMap ) : Entity = {
    var entityRoom : Entity = world.createEntity("ROOM"+roomId)
    var room : Room = new Room(roomId)

    entityRoom.components += room
    entityRoom.components += tileMap

    world.groups("ROOMS") += entityRoom
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
//       .foreach{j => println(compact(render(j)))}
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
      arrayTile(i%width)(i/width).layers += new NormalLayer(bundle.data(i))
      else {
      arrayTile(i%width)(i/width).layers += new CollidableLayer(bundle.data(i))
      // println("Height: " + (i%width) + " Row: " + (i/height) + " Value: " + bundle.data(i))
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
