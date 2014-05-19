package ayai.systems.mapgenerator

import ayai.maps.Tileset

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import scala.collection.mutable.ListBuffer

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
				// if(map(i) < 150 &&  map(i) > 0) {
				if(map(i) == 0) {
					newMap(i) = 121		//light water
				}
				// else if(map(i) == 0) {
				else if(map(i) == 1) {
					newMap(i) = 211;	//dark water
				}
				else {
					newMap(i) = 244;	//grass
				}

		}

		val list = newMap.toList

		case class Layer()
		// case class Tileset()

		val layers = List(Layer())
		// val tilesets = ListBuffer(Tileset())

		val tilesets: ListBuffer[Tileset] = ListBuffer()
    tilesets += new Tileset("overworld (1).png", "overworld (1)", height, width)

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
				("tilesets" -> (tilesets.toList map (_.asJson))) ~
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