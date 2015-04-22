package ayai.factories

/** Ayai Imports **/
import ayai.components._
import ayai.components.pathfinding.{ManhattanDistance, AStar}
import ayai.maps._
import ayai.actions._
import ayai.statuseffects._
import ayai.gamestate._
import ayai.networking.ConnectionWrite
import ayai.persistence.{CharacterTable, CharacterRow, InventoryTable}
import ayai.apps.Constants
import ayai.systems.JTransport

/** Crane Imports **/
import crane.{Entity, World}

/** External Imports **/
import java.io.File
import java.rmi.server.UID
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.mutable.{ListBuffer, ArrayBuffer}
import scala.io.Source

/** Akka Imports **/
import akka.actor.{ActorSystem, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout

object EntityFactory {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

  //Should take characterId: Long as a parameter instead of characterName
  //However can't do that until front end actually gives me the characterId
  def loadCharacter(world: World, entityId: String, characterName: String, x: Int, y: Int, actor: ActorSelection, networkSystem: ActorSystem): Unit = {
    CharacterTable.getCharacter(characterName) match {
      case Some(characterRow: CharacterRow) => {
        val p: Entity = world.createEntity(tag=entityId)
        val className = characterRow.className
        val classFuture = networkSystem.actorSelection("user/ClassMap") ? GetClassByName(className)

        // not used until we know if new character
        val classValues: ClassValues = Await.result(classFuture, timeout.duration).asInstanceOf[ClassValues]
        println(classValues)
        // calculate stats to add 
        val stats: Stats = new Stats()
        classValues.baseStats.stats.foreach(stat => {
          stats.addStat(new Stat(stat.attributeType, stat.magnitude+(stat.growth * characterRow.level), stat.growth))
        })
        p.components += stats

        p.components += new Position(characterRow.pos_x,characterRow.pos_y)
        p.components += new Bounds(28, 28)
        p.components += new Velocity(4, 4)
        val animations = new ArrayBuffer[Animation]()
        animations += new Animation("facedown", 1, 1 )
        animations += new Animation("faceleft", 4, 4 )
        animations += new Animation("faceright", 7, 7 )
        animations += new Animation("faceup", 10, 10 )
        animations += new Animation("walkdown", 0, 2 )
        animations += new Animation("walkleft", 3, 5 )
        animations += new Animation("walkright", 6, 8 )
        animations += new Animation("walkup", 9, 11)
        animations += new Animation("attackdown", 12, 15)
        animations += new Animation("attackleft",18, 21)
        animations += new Animation("attackright", 24, 27)
        animations += new Animation("attackup", 30, 33)
        val spritesheet: SpriteSheet = new SpriteSheet("guy", animations, 32 ,32)
        p.components += spritesheet
        p.components += new Actionable(false, DownDirection)
        val baseHealth = classValues.baseHealth + (characterRow.level * 20)
        val baseMana = classValues.baseMana+ (characterRow.level * 10)
        p.components += new Health(baseHealth,baseHealth)
        p.components += new NetworkingActor(actor)
        p.components += new Mana(baseMana, baseMana)
        p.components += new Experience(characterRow.experience, characterRow.level)
        p.components += new Room(characterRow.room_id)
        p.components += new Character(entityId, characterRow.name)
        p.components += new Faction("allies")

        p.components += new Vision(new WuLOS(), 2000)
        p.components += new Hearing(.5)
        p.components += new SoundProducing(3200)
        p.components += new Memory()

        p.components += new QuestHistory()

        // get stats needed for class

        val questbag = new QuestBag()
        val questSelection = networkSystem.actorSelection("user/QuestMap")
        // var future = questSelection ? GetQuest("QUEST1")
        // questbag.addQuest(Await.result(future, timeout.duration).asInstanceOf[Quest])
        // future = questSelection ? GetQuest("QUEST2")
        // questbag.addQuest(Await.result(future, timeout.duration).asInstanceOf[Quest])

        p.components += questbag

        val inventory = new Inventory()

        val itemSelection = networkSystem.actorSelection("user/ItemMap")

        InventoryTable.getInventory(p) foreach ((itemId: Long) => {
          val future = itemSelection ? GetItem(itemId.toString)
          inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])
        })

        // var future = itemSelection ? GetItem("ITEM" + itemId)
        // inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])

        val item = new Item(4, "Potion", 0, 20, new Consumable())

        item.effects += new Effect(0,"heal", "Heals for 50 hp", "currentHealth", 50, new OneOff(), new Multiplier(1.0))
        
        inventory.addItem(item)

        p.components += inventory

        val equipment = new Equipment()

        InventoryTable.getEquipment(p) foreach {
          case (slot: String, itemId: Long) => {
            val future = itemSelection ? GetItem(itemId.toString)
            equipment.equipItem(Await.result(future, timeout.duration).asInstanceOf[Item], slot)
          }
        }

        p.components += equipment

        world.addEntity(p)

        //I think that there should probably be a lookup based on the character's room here.
        val roomInfo = world.getEntityByTag("ROOM"+characterRow.room_id) match {
          case Some(e: Entity) => e
          case _ => null //load default here (too bored to implement now) <- thanks
        }

        val tileMap = world.asInstanceOf[RoomWorld].tileMap
        val mapFile = Source.fromFile(new File("src/main/resources/assets/maps/" + tileMap.file))
        val tileMapString = mapFile.mkString.filterNot({_ == "\n"})
        mapFile.close()

        val json =("type" -> "id") ~
          ("id" -> entityId) ~
          ("x" -> x) ~
          ("y" -> y) ~
          ("tilemap" -> tileMapString) ~
          ("tilesets" -> (tileMap.tilesets map (_.asJson))) ~
          (spritesheet.asJson)

        val actorSelection = networkSystem.actorSelection(s"user/SockoSender$entityId")
        actorSelection ! new ConnectionWrite(compact(render(json)))
      }

      case _ => {
        println(s"CHARACTER $characterName NOT FOUND!!!!!!!!!!")
        val actorSelection = networkSystem.actorSelection(s"user/SockoSender$entityId")
        actorSelection ! new ConnectionWrite(":(")
      }
    }
  }

  /**
  ** Create all NPCS given in npcs.json
  **/
  def createNPC(world: World, faction: String, npcValue: AllNPCValues, questBuffer: ArrayBuffer[Quest] = new ArrayBuffer[Quest]()): Entity = {
    val id = new UID().toString
    val p: Entity = world.createEntity(tag=id)
    p.components += new Position(npcValue.xposition, npcValue.yposition)
    p.components += new Bounds(32, 32)
    p.components += new Experience(npcValue.experience, npcValue.level)
    p.components += new Velocity(2, 2)
    p.components += new Actionable(false, DownDirection)
    p.components += new Health(npcValue.maximumHealth, npcValue.maximumHealth)
    val animations = new ArrayBuffer[Animation]()
    animations += new Animation("facedown",0,0)
    p.components += new SpriteSheet("npc", animations,32, 48)
    p.components += new Mana(npcValue.maximumMana,npcValue.maximumMana)
    p.components += new NPC(0)
    p.components += new Respawnable()
    p.components += new Room(npcValue.roomId)
    p.components += new Character(id, npcValue.name)
    p.components += new Faction(faction)

    p.components += new Vision(new WuLOS(), 2000)
    p.components += new Hearing(.5)
    p.components += new SoundProducing(2000)
    p.components += new Memory()

    p.components += new AStar(new ManhattanDistance)

    p.components += new QuestHistory()
    //p.components += new GenerateQuest()

    p
  }

  def createLoot(world: World, item: Item, networkSystem: ActorSystem, position: Position = new Position(100,100)): Entity =  {
    val id = (new UID()).toString
    val entity = world.createEntity(id)
    entity.components += new Loot(id, "")
    val inventory = new Inventory()
    inventory.addItem(item)
    entity.components += inventory
    entity.components += position
    val animations = new ArrayBuffer[Animation]()
    animations += new Animation("facedown", 0, 0)
    entity.components += new SpriteSheet("props", animations, 40, 40)
    entity
  }

  def createAI(world: World,
               faction: String,
               networkSystem: ActorSystem,
               position: Position = new Position(300,300), npcValue: NPCValues = null, monsterId: Int = 1, roomId: Int = 0): Entity = {
    val id = java.util.UUID.randomUUID.toString
    var name = id
    if (npcValue != null) {
      name = npcValue.name
    }

    val entity: Entity = world.createEntity(tag=id)
    entity.components += position
    entity.components += new Bounds(32, 32)
    entity.components += new Velocity(2, 2)
    entity.components += new Respawnable()
    entity.components += new Experience(500, 5)
    entity.components += new Actionable(false, DownDirection)
    entity.components += new Health(50, 50)
    val animations = new ArrayBuffer[Animation]()
    animations += new Animation("facedown", 1, 1 )
    animations += new Animation("faceleft", 4, 4 )
    animations += new Animation("faceright", 7, 7 )
    animations += new Animation("faceup", 10, 10 )
    animations += new Animation("walkdown", 0, 2 )
    animations += new Animation("walkleft", 3, 5 )
    animations += new Animation("walkright", 6, 8 )
    animations += new Animation("walkup", 9, 11)
    animations += new Animation("attackdown", 12, 15)
    animations += new Animation("attackleft",18, 21)
    animations += new Animation("attackright", 24, 27)
    animations += new Animation("attackup", 30, 33)
    val inventory = new Inventory()
    val itemSelection = networkSystem.actorSelection("user/ItemMap")
    val future = itemSelection ? GetItem("5")
    inventory.addItem(Await.result(future, timeout.duration).asInstanceOf[Item])
    entity.components += inventory
    entity.components += new SpriteSheet("guy", animations, 32 ,32)
    entity.components += new Mana(200, 200)
    entity.components += new Character(id, name)
    entity.components += new Goal
    entity.components += new NPC(0)
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
  case class JTilesets(image: String, name: String, imageheight: Int, imagewidth: Int)

  /**
  ** Load a room from a json file (based of tmx file from tiled) and then put in room tilemap
  **/
  def loadRoomFromJson(jsonFile: String, parsedJson: JValue): TileMap = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val tmap = parsedJson.extract[JTMap]
    val jtilesets = (parsedJson \\ "tilesets").extract[List[JTilesets]]
    val jtransports = (parsedJson \\ "transports").extract[List[JTransport]]
    var transports: List[TransportInfo] = Nil

    //get the overall size and id of maps
    val id: Int = tmap.id
    val height: Int = tmap.height
    val width: Int = tmap.width

    for(trans <- jtransports) {
      val startPosition = new Position(trans.start_x, trans.start_y)
      val endPosition = new Position(trans.end_x, trans.end_y)
      val toPosition = new Position(trans.to_x, trans.to_y)
      transports = new TransportInfo(startPosition, endPosition, id, trans.toRoomId, toPosition, trans.dir) :: transports
    }

    val bundles = (parsedJson \\ "layers").extract[List[JTiles]]
    bundles.map{bundle => ("data" -> bundle.data, "height" -> bundle.height, "width" -> bundle.width, "name" -> bundle.name)}
    val arrayTile: Array[Array[Tile]] = Array.fill[Tile](tmap.width, tmap.height)(new Tile(ListBuffer()))


    //get and transorm tiles from a list to multi-dimensional array
    for (i <- 0 until width * height) {
      for (bundle <- bundles) {
        if (bundle.data(i) != 0 ) {
          val relativePosition: Position = new Position((i % width) * 32 + 16, (i / width) * 32 + 16)
          arrayTile(i%width)(i/width).tilePosition = relativePosition
          arrayTile(i%width)(i/width).indexPosition = new Position(i % width,i / width)
          if (bundle.name != "collision") {
            arrayTile(i % width)(i / width).layers += new NormalLayer(bundle.data(i))
          } else {
            arrayTile(i % width)(i / width).layers += new CollidableLayer(bundle.data(i))
          }
        }
      }
    }
    //parse the tilesets and put them in a List[String]
    //by jarrad : THIS IS HORRID, REALLY NEED TO FIX THIS UP AND GET FEATURES WORKING FOR MAPS
    val tilesets: ListBuffer[Tileset] = ListBuffer()
    for(tileset <- jtilesets) {
      tilesets += new Tileset(tileset.image, tileset.name, tileset.imageheight, tileset.imagewidth)
    }
    // val tilesets = new Tilesets(images.toList)

    val tileMap: TileMap = new TileMap(arrayTile, transports, tilesets.toList)
    tileMap.height = tmap.height
    tileMap.width = tmap.width
    tileMap.file = jsonFile
    tileMap
    //new Entity
    //val entityRoom: Entity = createRoom(world, id, tileMap)
    //entityRoom
  }

  /**
  ** Take an entity and take its inventory and create a loot entity
  **/
  def characterToLoot(world: World, initiator: Entity, position: Position): Entity = {
    val id = new UID().toString
    val lootEntity: Entity = world.createEntity(tag=id)
    val animations = new ArrayBuffer[Animation]()
    animations += new Animation("facedown", 0, 0)
    lootEntity.components += new SpriteSheet("props", animations, 40, 40)

    lootEntity.components += new Loot(id, initiator.getComponent(classOf[Character]) match {
      case Some(character: Character) => character.id
      case _ => ""
    })

    initiator.getComponent(classOf[Inventory]) match {
      case (Some(inv: Inventory)) =>
        lootEntity.components += inv.copy()
        case _ =>
    }
    lootEntity.components += position
    lootEntity
  }
}
