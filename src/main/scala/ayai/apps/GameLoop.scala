package ayai.apps

/** Ayai Imports **/
import ayai.networking._
import ayai.components._
import ayai.persistence._
import ayai.gamestate.{Effect, EffectType, GameStateSerializer, CharacterRadius, MapRequest, RoomWorld}
import ayai.factories._

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, ActorSystem, Status, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

/** External Imports **/
import scala.concurrent.{Await, ExecutionContext, Promise, Future}
import scala.concurrent.duration._
import scala.collection.concurrent.{Map => ConcurrentMap, TrieMap}
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, HashMap}

import org.slf4j.{Logger, LoggerFactory}


object GameLoop {
  private val log = LoggerFactory.getLogger(getClass)

  var running: Boolean = true

  def main(args: Array[String]) {
    implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
    import ExecutionContext.Implicits.global

    DBCreation.ensureDbExists()

    var worlds = HashMap[String, RoomWorld]()
    var socketMap: ConcurrentMap[String, String] = TrieMap[String, String]()

    val networkSystem = ActorSystem("NetworkSystem")
    val nmQueue = networkSystem.actorOf(Props[NetworkMessageQueue], name="NMQueue")
    val nmInterpreter = networkSystem.actorOf(Props[NetworkMessageInterpreterSupervisor], name="NMInterpreter")
    val aProcessor = networkSystem.actorOf(Props[AuthorizationProcessor], name="AProcessor")

    val rooms = List("map3", "map2")
    val worldFactory = WorldFactory(networkSystem)

    for((file, index) <- rooms.zipWithIndex)
      worlds.add(s"$index", worldFactory.createWorld(s"room$index", s"$file"))

    val receptionist = SockoServer(networkSystem)
    receptionist.run(Constants.SERVER_PORT)

    //GAME LOOP RUNS AS LONG AS SERVER IS UP
    while(running) {
      val start = System.currentTimeMillis

      val future = nmQueue ? FlushMessages
      val result = Await.result(future, timeout.duration).asInstanceOf[QueuedMessages]

      val processedMessages = new ArrayBuffer[Future[Any]]
      result.messages.foreach { message =>
        processedMessages += world.nmProcessor ? new ProcessMessage(message)
      }

      Await.result(Future.sequence(processedMessages), 1 seconds)

      for((name, world) <- worlds)
        world.process()

      val end = System.currentTimeMillis
      if((end - start) < (1000 / Constants.FRAMES_PER_SECOND)) {
        Thread.sleep((1000 / Constants.FRAMES_PER_SECOND) - (end - start))
      }
    }
  }
}


