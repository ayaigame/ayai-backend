package ayai.systems.mapgenerator

import ayai.factories.{WorldFactory, CreateWorld}
import ayai.gamestate.RoomWorld
import ayai.apps.Constants

import java.util.Random
import java.io._
import scala.collection.immutable.Range

/** Akka Imports **/
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Status.{Success, Failure}
import akka.pattern.ask
import akka.util.Timeout

/** External Imports **/
import scala.concurrent.{Await, ExecutionContext, Promise, Future}
import scala.concurrent.duration._

//width and height are in terms of tiles.
case class CreateMap(id: Int, width: Int, height: Int)
case class NewRoomWorld(world: RoomWorld)

//This Actor will write to a map.json file and return the name of the file.
//The WorldGenerator will then be able to use that file to instantiate the room.
class MapGenerator extends Actor {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  //TODO:
  //Write to mapsList.json file so created worlds get loaded in when the server starts
  //Add transports to the maps so they will continue

  def rescaleNoise(noise: Array[Array[Double]]) : Array[Array[Int]] = {
    def getMax(row: Array[Double]): Double = {row reduceLeft (_ max _)}
    def getMin(row: Array[Double]): Double = {row reduceLeft (_ min _)}
    val noiseMax = noise map getMax reduceLeft (_ max _)
    val noiseMin = noise map getMin reduceLeft (_ min _)

    val range = noiseMax - noiseMin

    val numLevels = 3
    val brackets = Range.Double(noiseMin, noiseMax, range/(numLevels - 1))

    def rescaleTile(x: Double): Int = {
      //Find the bracket the tile falls into
      brackets.zipWithIndex.find(_._1 >= x).getOrElse((0.0,0))._2
    }

    def rescaleRow(row: Array[Double]): Array[Int] = {
      row map rescaleTile
    }

    noise map rescaleRow
 }

  def receive = {
    case CreateMap(id: Int, width: Int, height: Int) => {
      val noiseGenerator = FractionalBrownianMotionGenerator(NoiseGenerator("perlin"))

      val noise2d = noiseGenerator.getNoise(width, height)

      // println(rescaleNoise(noise2d).map(_.deep.mkString(" ")).mkString("\n"))

      val rescaledNoise = rescaleNoise(noise2d)

      val fileName = TiledExporter.export(id, rescaledNoise, width, height)

      val worldFactory = context.system.actorSelection("user/WorldFactory")
      val future = worldFactory ? new CreateWorld(fileName)

      // sender ! new NewRoomWorld(Await.result(future, timeout.duration).asInstanceOf[RoomWorld])
      sender ! Await.result(future, timeout.duration)
    }

    case _ => println("Error: from MapGenerator.")
      sender ! Failure
  }
}