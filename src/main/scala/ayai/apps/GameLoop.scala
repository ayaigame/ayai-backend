package ayai.apps

/** Ayai Imports **/
import ayai.networking._
import ayai.persistence._
import ayai.gamestate._
import ayai.factories._
import ayai.systems.mapgenerator.{WorldGenerator, ExpandRoom}

/** Akka Imports **/
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

/** External Imports **/
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.concurrent.{Map => ConcurrentMap, TrieMap}
import scala.collection.mutable.ArrayBuffer

import java.io.{File, FileWriter, BufferedWriter}

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import org.slf4j.LoggerFactory

/**
 ** The main loop of the Ayai Game
 ** First loads all needed Actors, creates needed worlds, and runs the game loop
 **/
object GameLoop {

  private lazy val log = LoggerFactory.getLogger(getClass)

  var running: Boolean = true

  def main(args: Array[String]) {
    implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)

    // COMMENT ME OUT TO SPEED UP BOOTUP TIME
    //This recreates the database and mapList.json file.
    DBCreation.ensureDbExists()
    val fwMapList = new FileWriter(new File("src/main/resources/assets/maps/mapList.json"))
    val bwMapList = new BufferedWriter(fwMapList)

    bwMapList.write(pretty(render(List("map0.json", "map1.json"))))
    bwMapList.close()
    fwMapList.close()

    var socketMap: ConcurrentMap[String, String] = TrieMap[String, String]()

    val networkSystem = ActorSystem("NetworkSystem")
    val mQueue = networkSystem.actorOf(Props[MessageQueue], name="MQueue")
    val nmInterpreter = networkSystem.actorOf(Props[NetworkMessageInterpreterSupervisor], name="NMInterpreter")
    val aProcessor = networkSystem.actorOf(Props(new AuthorizationProcessor(networkSystem)), name="AProcessor")
    val socketUserMap = networkSystem.actorOf(Props[SocketUserMap], name="SocketUserMap")
    val userRoomMap = networkSystem.actorOf(Props[UserRoomMap], name="UserRoomMap")
    val roomList = networkSystem.actorOf(Props[RoomList], name="RoomList")
    val itemMap = networkSystem.actorOf(Props[ItemMap], name="ItemMap")
    val classMap = networkSystem.actorOf(Props[ClassMap], name="ClassMap")
    val questMap = networkSystem.actorOf(Props[QuestMap], name="QuestMap")
    val worldFactory = networkSystem.actorOf(Props[WorldFactory], name="WorldFactory")
    val worldGenerator = networkSystem.actorOf(Props[WorldGenerator], name="WorldGenerator")
    val effectMap = networkSystem.actorOf(Props[EffectMap], name="EffectMap")
    val spriteSheetMap = networkSystem.actorOf(Props[SpriteSheetMap], name="SpriteSheetMap")
    val npcMap = networkSystem.actorOf(Props[NPCMap], name="NPCMap")

    //Read in list of map files.
    implicit val formats = net.liftweb.json.DefaultFormats
    val roomSource = scala.io.Source.fromFile("src/main/resources/assets/maps/mapList.json")
    val roomLines = roomSource.mkString
    roomSource.close()

    val rooms = parse(roomLines).extract[List[String]]
    // val rooms = List("map0.json", "map1.json")

    val itemFactory = ItemFactory.bootup(networkSystem)
    val questFactory = QuestFactory.bootup(networkSystem)
    val classFactory = ClassFactory.bootup(networkSystem)
    val effectFactory = EffectFactory.bootup(networkSystem)

    for (file <- rooms) {
      val future = worldFactory ? new CreateWorld(file)
      val result = Await.result(future, timeout.duration).asInstanceOf[RoomWorld]
      roomList ! AddWorld(result)
    }

    //Ensure the first room is expanded, since its expansion will not be
    //triggered by the transport system. (Since you don't transport into it.)
    val futureOfRoom0 = roomList ? GetWorldById(0)
    Await.result(futureOfRoom0, timeout.duration) match {
      case Some(room: RoomWorld) =>
        worldGenerator ! ExpandRoom(room)
      case _ => println("Cannot find room 0.")
    }

    val npcFactory = NPCFactory.bootup(networkSystem)

    val receptionist = SockoServer(networkSystem)
    receptionist.run(Constants.SERVER_PORT)

    //GAME LOOP RUNS AS LONG AS SERVER IS UP
    while (running) {
      val start = System.currentTimeMillis

      val processedMessages = new ArrayBuffer[Future[Any]]
      val futureWorlds = roomList ? new GetAllWorlds()
      val worlds = Await.result(futureWorlds, timeout.duration).asInstanceOf[ArrayBuffer[RoomWorld]]
      for (world <- worlds) {
        val id = world.id
        val future = mQueue ? FlushMessages(id)
        val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]
        val mProcessor = networkSystem.actorSelection(s"user/MProcessor$id")

        result.messages.foreach { message =>
          processedMessages += mProcessor ? new ProcessMessage(message)
        }
      }

      Await.result(Future.sequence(processedMessages), 10 seconds)

      for (world <- worlds) {
        world.process()
      }

      val end = System.currentTimeMillis
      if ((end - start) < (1000 / Constants.FRAMES_PER_SECOND)) {
        Thread.sleep((1000 / Constants.FRAMES_PER_SECOND) - (end - start))
      }
    }
  }
}

