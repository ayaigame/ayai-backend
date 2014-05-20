package ayai.systems.mapgenerator

import ayai.factories.{WorldFactory, CreateWorld}
import ayai.gamestate.{RoomWorld, GetTransportsToRoom}
import ayai.apps.Constants
import ayai.maps.TransportInfo
import ayai.systems.JTransport
import ayai.components.Position

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

    // def rescaleTile(x: Double): Int = {
    //   //Find the bracket the tile falls into
    //   brackets.zipWithIndex.find(_._1 >= x).getOrElse((0.0,0))._2
    // }

    def rescaleTile(x: Double) : Int = {
      var temp = 0.0;
      if (x > 0)
        temp = x / (noiseMax * 2)
      else
        temp = x / (noiseMin * -2)

      ((temp + 0.5) * 3).toInt
    }

    def rescaleRow(row: Array[Double]): Array[Int] = {
      row map rescaleTile
    }

    noise map rescaleRow
  }

  def findTransportLocation(direction: String, map: Array[Array[Int]], width: Int, height: Int): Position = {
    val transportThickness = 3
    val transportLength = 5

    var widthOfTransport = transportThickness
    var heightofTransport = transportLength
    var startX = 0
    var startY = 0
    var incrX = 0
    var incrY = 0
    var transportAlignment = 0

    //These are inverted because these are incoming directions
    direction match {
      case "RightToLeft" =>
        incrY = 1

      case "LeftToRight" =>
        startX = width-transportThickness
        transportAlignment = transportThickness - 1
        incrY = 1

      case "BottomToTop" =>
        incrX = 1
        widthOfTransport = transportLength
        heightofTransport = transportThickness

      case "TopToBottom" =>
        startY = height-transportThickness
        incrX = 1
        widthOfTransport = transportLength
        heightofTransport = transportThickness

      case _ => {
        println("MISSING TRANSPORT DIRECTION IN MapGenerator.")
        incrX = 1 //Otherwise the following while loop will never end
      }
    }

    while((startX + widthOfTransport) <= width && (startY + heightofTransport) <= height) {
      var noCollision = true

      for(m <- startX until (startX + widthOfTransport)) {
        for(n <- startY until (startY + heightofTransport)) {
          if(map(m)(n) != 1)
            noCollision = false
        }
      }

      //+incr is a trick to give a buffer of clear non-transport tile
      if(noCollision)
        return new Position(startX+transportAlignment, startY)

      startX = startX + incrX
      startY = startY + incrY
    }

    //No valid position found
    return new Position(-1, -1)
  }

  // def findFurtherestTransportLocation(direction: String,
  //                                     map: Array[Array[Int]],
  //                                     id: Int,
  //                                     width: Int,
  //                                     height: Int,
  //                                     fromX: Int,
  //                                     fromY: Int
  //                                   ): Position = {

  // }

  def doesPathExist(startX: Int, startY: Int, endX: Int, endY: Int, map: Array[Array[Int]], width: Int, height: Int): Boolean = {
    // println(s"Finding path from ($startX, $startY) to ($endX, $endY).")
    var blockedIn = false
    var currentX = startX
    var currentY = startY
    var incrX = 0
    var incrY = 0

    //Set direction to bias
    if(startX < endX)
      incrX = 1
    else
      incrX = -1

    if(startY < endY)
      incrY = 1
    else
      incrY = -1

    while(blockedIn == false) {
      if(currentX == endX && currentY == endY)
        return true

      if(currentX+incrX > 0 && currentX+incrX < width && map(currentX+incrX)(currentY) != 0){
        map(currentX)(currentY) = map(currentX)(currentY) - 1
        currentX = currentX+incrX
      }
      else if(currentY+incrY > 0 && currentY+incrY < height && map(currentX)(currentY+incrY) != 0){
        map(currentX)(currentY) = map(currentX)(currentY) - 1
        currentY = currentY+incrY
      }
      else if(currentX-incrX > 0 && currentX-incrX < width && map(currentX-incrX)(currentY) != 0){
        map(currentX)(currentY) = map(currentX)(currentY) - 1
        currentX = currentX-incrX
      }
      else if(currentY-incrY > 0 && currentY-incrY < height && map(currentX)(currentY-incrY) != 0){
        map(currentX)(currentY) = map(currentX)(currentY) - 1
        currentY = currentY-incrY
      }
      else
        blockedIn = true
    }

    return false
  }

  //Creates a map of tiles where each tile's value is equal to how many
  //non-colllidable neighbors it has. Collidable tiles value is 0.
  def calculateCollisionMap(map: Array[Array[Int]], width: Int, height: Int): Array[Array[Int]] = {
    var newMap = Array.ofDim[Int](width, height)
    val nonCollisionTile = 1

    for(i <- 0 until width) {
      for(j <- 0 until height) {
        if(map(i)(j) == nonCollisionTile) {
          var tileVal = 0
          if(i+1 < width && map(i+1)(j) ==  nonCollisionTile)
            tileVal = tileVal + 1
          if(j+1 < height && map(i)(j+1) ==  nonCollisionTile)
            tileVal = tileVal + 1
          if(i-1 >= 0 && map(i-1)(j)==  nonCollisionTile)
            tileVal = tileVal + 1
          if(j-1 >= 0 && map(i)(j-1) ==  nonCollisionTile)
            tileVal = tileVal + 1
          newMap(i)(j) = tileVal
        }
        else
          newMap(i)(j) = 0
      }
    }

    return newMap
  }

  def ensureTraversable(map: Array[Array[Int]], transportPositions: List[Position], width: Int, height: Int): Boolean = {
    var remainingList = transportPositions
    val collisionMap: Array[Array[Int]] = calculateCollisionMap(map, width, height)

    while(remainingList.nonEmpty) {
      val first = remainingList(0)
      val rest = remainingList.tail
      for(position <- rest) {
        if(!doesPathExist(first.x, first.y, position.x, position.y, collisionMap, width, height)) {
          return false
        }
      }
      remainingList = rest
    }

    return true
  }

  def receive = {
    case CreateMap(id: Int, width: Int, height: Int) => {
      val noiseGenerator = FractionalBrownianMotionGenerator(NoiseGenerator("perlin"))

      // println(findTransportLocation("RightToLeft", rescaledNoise, width, height))

      val roomList = context.system.actorSelection("user/RoomList")
      val roomListFuture = roomList ? new GetTransportsToRoom(id)

      var transportsToRoom: List[TransportInfo] =  Await.result(roomListFuture, timeout.duration).asInstanceOf[List[TransportInfo]]

      //Add the outbound transport, this is inverted since it's incoming
      val outboundTransport = new TransportInfo(new Position(0, 0), new Position(1, 5), id+1, id, new Position(100, 100), "LeftToRight")
      transportsToRoom = transportsToRoom ::: List(outboundTransport)
      // incomingTransportDirections = incomingTransportDirections ::: List("LeftToRight")

      var incomingTransportDirections = transportsToRoom map (_.direction)

      var noise2d = noiseGenerator.getNoise(width, height)
      var rescaledNoise = rescaleNoise(noise2d)

      var noPositionFound = new Position(-1, -1)
      var transportPositions: List[Position] = incomingTransportDirections map (findTransportLocation(_, rescaledNoise, width, height))

      while(transportPositions.exists(_ == noPositionFound)
          || !ensureTraversable(rescaledNoise, transportPositions, width, height)) {
        println("Whelp that room's untraversable. Lemme make another...")
        noise2d = noiseGenerator.getNoise(width, height)
        rescaledNoise = rescaleNoise(noise2d)

        transportPositions = incomingTransportDirections map (findTransportLocation(_, rescaledNoise, width, height))
      }

      // println(rescaledNoise.map(_.deep.mkString(" ")).mkString("\n"))
      // println(ensureTraversable(rescaledNoise, transportsToRoom, width, height))
      val jTransports: List[JTransport] = TransportFactory.createTransports(id, transportsToRoom.zip(transportPositions), rescaledNoise, width, height)
      val fileName = TiledExporter.export(id, jTransports, rescaledNoise, width, height)

      val worldFactory = context.system.actorSelection("user/WorldFactory")
      val future = worldFactory ? new CreateWorld(fileName)

      // sender ! new NewRoomWorld(Await.result(future, timeout.duration).asInstanceOf[RoomWorld])
      sender ! Await.result(future, timeout.duration)
    }

    case _ => println("Error: from MapGenerator.")
      sender ! Failure
  }
}