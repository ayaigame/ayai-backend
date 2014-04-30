package ayai.systems.mapgenerator

import java.util.Random
import java.io._
import scala.collection.immutable.Range

object MapGenerator {
  //TODO:
  //Write to mapsList.json file so created worlds get loaded in when the server starts
  //Add transports to the maps so they will continue




  //width and height are in terms of tiles.
  def getMap(name: String, width: Int, height: Int) {

    // val writer = new PrintWriter(name + ".json")
    val writer = new PrintWriter("src/main/resources/assets/maps/" + name + ".json")

    writer.print("{\n  \"id\": 1,\n  \"orientation\":\"orthogonal\",\n  \"properties\": {},\n")
    writer.print("  \"height\":" + height + ",\n  \"width\":" + width + ",\n")
    writer.print("  \"tilewidth\":32,\n  \"version\":1,\n  \"tileheight\":32,\n")
    writer.print("  \"transports\" : [],\n")
    writer.print("  \"tilesets\":[{\n    \"firstgid\":1,\n    \"image\":\"..\\/tiles\\/tiles.png\",\n")
    writer.print("    \"imageheight\":192,\n    \"imagewidth\":192,\n    \"margin\":0,\n    \"name\":\"tiles\",\n")
    writer.print("    \"properties\": {},\n    \"spacing\":0,\n    \"tileheight\":32,\n    \"tilewidth\":32\n    }],\n")
    writer.print("  \"layers\":[{\n    \"name\":\"Tile Layer 1\",\n    \"opacity\":1,\n")
    writer.print("    \"height\":" + height + ",\n    \"width\":"+ width + ",\n")
    writer.print("    \"type\":\"tilelayer\",\n    \"visible\":true,\n    \"x\":0,\n    \"y\":0,\n")
    writer.print("    \"data\":[")
    writeNoise(width, height, writer)
    writer.print("  }]\n}")
    writer.close()
  }

  //Takes a noise value and translates it to the corresponding tile id
  def translateTile(i: Int): Int = {
    if(i == 0)
      375
    else if(i == 1)
      4
    else
      12
  }

  def writeNoise(width: Int, height: Int, writer: PrintWriter) {
    val noise = NoiseGenerator.getNoise("perlin", width, height, 1)
    for(i <- 0 to noise.length-1) {
      for(j <- 0 to noise(i).length-1){
        if(i == noise.length-1 && j == noise(i).length-1)
          //last tile should close the list instead of adding a comma
          writer.print(translateTile(noise(i)(j)) + "]\n")
        else
          writer.print(translateTile(noise(i)(j)) + ", ")
      }
    }
  }
}