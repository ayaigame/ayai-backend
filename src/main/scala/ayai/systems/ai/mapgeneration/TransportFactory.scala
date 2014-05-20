package ayai.systems.mapgenerator

import ayai.maps.Tileset
import ayai.systems.JTransport
import ayai.maps.TransportInfo
import ayai.components.Position

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import scala.collection.mutable.ListBuffer
import scala.math.min

import net.liftweb.json._;
import net.liftweb.json.JsonDSL._;

object TransportFactory {

	def createTransports(id: Int,
          						 toTransports: List[(TransportInfo, Position)],
          						 map: Array[Array[Int]],
          						 width: Int,
          						 height: Int
          					): List[JTransport] = {
		var transports = new ListBuffer[JTransport]()

    //Transport to the next room
		// transports += new JTransport(width-1, 5, width, min(10, height), id+1, 100, 100, "RightToLeft")

    //Create a transport to get back to each transport pointing here.
    for((transport, position) <- toTransports){
      var newDirection = ""
      var toX = 100
      var toY = 100
      var endXOffset = 1
      var endYOffset = 10

      transport.direction match {
        case "LeftToRight" =>
          newDirection = "RightToLeft"
          endXOffset = 1
          endYOffset = 5
          toX = transport.endingPosition.x*32 + 64
          toY = (transport.startingPosition.y*32 + transport.endingPosition.y*32) / 2

        case "RightToLeft" =>
          newDirection = "LeftToRight"
          endXOffset = 1
          endYOffset = 5
          toX = transport.startingPosition.x*32 - 2
          toY = (transport.startingPosition.y + transport.endingPosition.y) * 32 / 2

        case "TopToBottom" =>
          newDirection = "BottomToTop"
          endXOffset = 5
          endYOffset = 1
          toX = (transport.startingPosition.x*32 + transport.endingPosition.x*32) / 2
          toY = transport.endingPosition.y*32 + 64

        case "BottomToTop" =>
          newDirection = "TopToBottom"
          endXOffset = 5
          endYOffset = 1
          toX = (transport.startingPosition.x*32 + transport.endingPosition.x*32) / 2
          toY = transport.startingPosition.y*32 - 64

        case _ => println("MISSING TRANSPORT DIRECTION IN TiledExporter.")
      }

      transports += new JTransport(position.x,
                                    position.y,
                                    position.x + endXOffset,
                                    position.y + endYOffset,
                                    transport.fromRoomId,
                                    toX,
                                    toY,
                                    newDirection)
    }
    	// var newDirection = ""
    	// var toX = 100
    	// var toY = 100
    	// var startX = 0
    	// var endX = 1
    	// var startY = 5
    	// var endY = 10

     //  //directions.indexWhere()
    	// transport.direction match {
    	// 	case "LeftToRight" =>
    	// 		newDirection = "RightToLeft"
    	// 		toX = transport.endingPosition.x*32 + 64
    	// 		toY = (transport.startingPosition.y*32 + transport.endingPosition.y*32) / 2
		   //  	startX = width-1
		   //  	endX = width
		   //  	startY = 5
		   //  	endY = 10

    	// 	case "RightToLeft" =>
    	// 		newDirection = "LeftToRight"
    	// 		toX = transport.startingPosition.x*32 - 2
    	// 		toY = (transport.startingPosition.y + transport.endingPosition.y) * 32 / 2
		   //  	startX = 0
		   //  	endX = 1
		   //  	startY = 5
		   //  	endY = 10

    	// 	case "TopToBottom" =>
    	// 		newDirection = "BottomToTop"
    	// 		toX = (transport.startingPosition.x*32 + transport.endingPosition.x*32) / 2
    	// 		toY = transport.endingPosition.y*32 + 64
		   //  	startX = 5
		   //  	endX = 10
		   //  	startY = height - 1
		   //  	endY = height

    	// 	case "BottomToTop" =>
    	// 		newDirection = "TopToBottom"
    	// 		toX = (transport.startingPosition.x*32 + transport.endingPosition.x*32) / 2
    	// 		toY = transport.startingPosition.y*32 - 64
		   //  	startX = 0
		   //  	endX = 1
		   //  	startY = 0
		   //  	endY = 1

    	// 	case _ => println("MISSING TRANSPORT DIRECTION IN TiledExporter.")
    	// }

    // 	transports += new JTransport(startX, startY, endX, endY, transport.fromRoomId, toX, toY, newDirection)
    // }

    transports.toList
  }
}