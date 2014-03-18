package ayai.factories

/** Ayai Imports **/
import ayai.components._
import ayai.maps._
import ayai.actions._
import ayai.gamestate._
import ayai.networking.ConnectionWrite
import ayai.actions.MoveDirection
import ayai.persistence.{AyaiDB, CharacterRow}
import ayai.apps.Constants

/** Crane Imports **/
import crane.Component
import crane.Entity
import crane.World


/** External Imports **/
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import scala.io.Source

/** Akka Imports **/
import akka.actor.{Actor, ActorSystem, ActorRef, Props, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import java.rmi.server.UID
import ayai.apps._

object EntityFactory {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  //Should take characterId: Long as a parameter instead of characterName
  //However can't do that until front end actually gives me the characterId
  def loadCharacter(world: World, entityId: String, characterName: String, x: Int, y: Int, actor: ActorSelection, networkSystem: ActorSystem) = {
    AyaiDB.getCharacter(characterName) match {
      case Some(characterRow: CharacterRow) =>
        val p: Entity = world.createEntity(tag=entityId)
        p.components += new Position(characterRow.pos_x,characterRow.pos_y)
        p.components += new Bounds(32, 32)
        p.components += new Velocity(4, 4)
        p.components += new Actionable(false, DownDirection)
        p.components += new Health(100,100)
        p.components += new NetworkingActor(actor)
        p.components += new Mana(200,200)
        p.components += new Room(characterRow.room_id)
        p.components += new Character(entityId, characterRow.name, characterRow.experience)
        p.components += new Faction("allies")

        val questbag = new QuestBag()
        val questSelection = networkSystem.actorSelection("user/QuestMap")
        var future = questSelection ? GetQuest("QUEST1")
        questbag.addQuest(Await.result(future, timeout.duration).asInstanceOf[Quest])
        future = questSelection ? GetQuest("QUEST2")
        questbag.addQuest(Await.result(future, timeout.duration).asInstanceOf[Quest])

        p.components += questbag

        val inventory = new Inventory()

        val itemSelection = networkSystem.actorSelection("user/ItemMap")

        future = itemSelection ? GetItem("ITEM1")
        inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])

        future = itemSelection ? GetItem("ITEM2")
        inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])
        future = itemSelection ? GetItem("ITEM1")
        inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])
        future = itemSelection ? GetItem("ITEM0")
        inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])

        p.components += inventory

        val equipment = new Equipment()
        p.components += equipment

        world.addEntity(p)

        //I think that there should probably be a lookup based on the character's room here.
        val roomInfo = world.getEntityByTag("ROOM"+characterRow.room_id) match {
          case Some(e: Entity) =>
            e
          case _ => //load default here (too bored to implement now)
            null
        }

        val tileMap = world.asInstanceOf[RoomWorld].tileMap


        val json = (
          ("type" -> "id") ~
          ("id" -> entityId) ~
          ("x" -> x) ~
          ("y" -> y) ~
          ("tilemap" -> tileMap.file) ~
          (tileMap.tilesets.asJson)

        )

        val actorSelection = networkSystem.actorSelection(s"user/SockoSender$entityId")
        actorSelection ! new ConnectionWrite(compact(render(json)))

      case _ =>
        println("CHARACTER NOT FOUND!!!!!!!!!!")
        val actorSelection = networkSystem.actorSelection(s"user/SockoSender$entityId")
        actorSelection ! new ConnectionWrite(":(")
    }
  }

  def createNPC(world: World, faction: String, npcValue: AllNPCValues, questBuffer: ArrayBuffer[Quest] = new ArrayBuffer[Quest]()): Entity = {
    val id = (new UID()).toString
    val p: Entity = world.createEntity(tag=id)
    p.components += new Position(npcValue.xposition, npcValue.yposition)
    p.components += new Bounds(32, 32)
    p.components += new Velocity(2, 2)
    p.components += new Actionable(false, DownDirection)
    p.components += new Health(npcValue.maximumHealth, npcValue.maximumHealth)
    p.components += new Mana(1,1)
    p.components += new NPC(0)
    p.components += new Respawnable()
    p.components += new Room(npcValue.roomId)
    p.components += new Character(id, npcValue.name, 0)
    p.components += new Faction("allies")
    p
  }

  def createAI(world: World, faction: String): Entity = {
    val name = java.util.UUID.randomUUID.toString
    val entity: Entity = world.createEntity(tag=name)
    entity.components += new Position(400, 400)
    entity.components += new Bounds(32, 32)
    entity.components += new Velocity(2, 2)
    entity.components += new Actionable(false, DownDirection)
    entity.components += new Health(50, 50)
    entity.components += new Mana(200, 200)
    entity.components += new Character(name, name, 0)
    entity.components += new Goal
    entity.components += new Faction(faction)
    entity.components += new Room(0)
    entity.components += new Equipment()

    entity
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
  //def createRoom(world: World, roomId: Int, tileMap: TileMap ): Entity = {
  //  var entityRoom: Entity = world.createEntity("ROOM"+roomId)
  //  var room: Room = new Room(roomId)

  //  entityRoom.components += room
  //  entityRoom.components += tileMap

  //  world.groups("ROOMS") += entityRoom
  //  //world.getManager(classOf[TagManager]).register(roomId.toString,entityRoom)

  //  entityRoom
  //}


  case class JTMap(id: Int, width: Int, height: Int)
  case class JTiles(data: List[Int], width: Int, height: Int, name: String)
  case class JTransport(start_x: Int, start_y: Int, end_x: Int, end_y: Int, toRoomFile: String, toRoomId: Int) {
    override def toString() : String = "start_x: " + start_x + " toRoomFile: " + toRoomFile
  }
  case class JTilesets(image: String)

  def loadRoomFromJson(jsonFile: String): TileMap = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val file = Source.fromURL(getClass.getResource("/assets/maps/" + jsonFile))
    val lines = file.mkString
    file.close()

    val parsedJson = parse(lines)
    val tmap = parsedJson.extract[JTMap]
    val jtilesets = (parsedJson \\ "tilesets").extract[List[JTilesets]]
    val jtransports = (parsedJson \\ "transports").extract[List[JTransport]]
    var transports: List[TransportInfo] = Nil

    for(trans <- jtransports) {
      val startPosition = new Position(trans.start_x, trans.start_y)
      val endPosition = new Position(trans.end_x, trans.end_y)
      transports = new TransportInfo(startPosition, endPosition, trans.toRoomFile, trans.toRoomId) :: transports
    }
    val bundles = (parsedJson \\ "layers").extract[List[JTiles]]
    bundles.map{bundle => ("data" -> bundle.data, "height" -> bundle.height, "width" -> bundle.width, "name" -> bundle.name)}
    val arrayTile: Array[Array[Tile]] = Array.fill[Tile](tmap.width, tmap.height)(new Tile(ListBuffer()))
    //get the overall size and id of maps
    val id: Int = tmap.id
    val height: Int = tmap.height
    val width: Int = tmap.width
    //get and transorm tiles from a list to multi-dimensional array
    for(i <- 0 until (width*height)) {
      for(bundle <- bundles) {
        if(bundle.data(i) != 0 ) {
          val relativePosition: Position = new Position((i % width)*32+16,(i / width)*32+16)
          arrayTile(i%width)(i/width).tilePosition = relativePosition
          arrayTile(i%width)(i/width).indexPosition = new Position(i % width,i / width)
          if(bundle.name != "collision") {
            arrayTile(i % width)(i / width).layers += new NormalLayer(bundle.data(i))
          }
          else {
            arrayTile(i % width)(i / width).layers += new CollidableLayer(bundle.data(i))
          }
        }
      }
    }
    //parse the tilesets and put them in a List[String]
    //by jarrad : THIS IS HORRID, REALLY NEED TO FIX THIS UP AND GET FEATURES WORKING FOR MAPS
    val images: ListBuffer[String] = ListBuffer()
    for(tileset <- jtilesets) {
      images += tileset.image
    }
    val tilesets = new Tilesets(images.toList)

    val tileMap: TileMap = new TileMap(arrayTile, transports, tilesets)
    tileMap.height = tmap.height
    tileMap.width = tmap.width
    tileMap.file = jsonFile
    tileMap
    //new Entity
    //val entityRoom: Entity = createRoom(world, id, tileMap)
    //entityRoom
  }


  def characterToLoot(initiator: Entity, lootEntity: Entity) {
      lootEntity.components += new NPC(0)
      lootEntity.components += new Loot(initiator.getComponent(classOf[Character]) match {
        case Some(character: Character) => character.id 
        case _ => "0"
      })

      initiator.getComponent(classOf[Inventory]) match {
        case (Some(inv: Inventory)) =>
          lootEntity.components += inv.copy()
          case _ => 
      }
  }
}
