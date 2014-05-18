package ayai.systems.mapgenerator

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import net.liftweb.json._;
import net.liftweb.json.JsonDSL._;

object TiledExporter {

	//returns filename
	def export(id: Int, map: Array[Int], width: Int, height: Int): String = {
		var newMap : Array[Int] = new Array[Int](width * height)
		// val name = System.currentTimeMillis() + ".json"; // this should prolly be the actual seed value instead
		val name = "map" + id + ".json"

		println("saving " + width + "x" + height +" map to "+name);

		for(i <- 0 until width * height) {
				if(map(i) < 150 &&  map(i) > 0) {
					newMap(i) = 121		//light water
				}
				else if(map(i) == 0) {
					newMap(i) = 211;	//dark water
				}
				else {
					newMap(i) = 244;	//grass
				}

		}

		val list = newMap.toList

		case class Layer()
		case class Tileset()

		val layers = List(Layer())
		val tilesets = List(Tileset())

		val json = (
				("id" -> id) ~
				("height" -> height) ~
				("width" -> width) ~
				("layers" -> layers.map { item => (
					("height" -> height) ~
					("data" -> list.reverse) ~
					("name" -> "Tile Layer 1") ~
					("opacity" -> 1) ~
					("type" -> "tilelayer") ~
					("visible" -> true) ~
					("width" -> width) ~
					("x" -> 0) ~
					("y" -> 0))}
					) ~
				("orientation" -> "orthogonal") ~
				("properties" -> null) ~
				("tileheight" -> 32) ~
				("tilesets" -> tilesets.map { item => (
					("firstgid" -> 1) ~
					("image" -> "sd33.png") ~
					("imageheight" -> 640) ~
					("imagewidth" -> 1184) ~
					("margin" -> 0) ~
					("name" -> "sd33") ~
					("properties" -> null) ~
					("spacing" -> 0) ~
					("tileheight" -> 32) ~
					("tilewidth" -> 32) ~
					("transparentcolor" -> "#ff00cc"))}
					) ~
				("tilewidth" -> 32) ~
				("version" -> 1) ~
				("transports" -> List[Int]())
			)

		var fw = new FileWriter(new File("src/main/resources/assets/maps/" + name))
		var bw = new BufferedWriter(fw)

		bw.write(pretty(render(json)))
		bw.close()
		fw.close()

		return name

	}
}