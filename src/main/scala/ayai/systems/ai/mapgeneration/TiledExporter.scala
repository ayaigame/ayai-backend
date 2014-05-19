package ayai.systems.mapgenerator

import ayai.maps.Tileset
import ayai.systems.JTransport

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import scala.collection.mutable.ListBuffer
import scala.math.min

import net.liftweb.json._;
import net.liftweb.json.JsonDSL._;

object TiledExporter {

	//returns filename
	def export(id: Int, map: Array[Array[Int]], width: Int, height: Int): String = {
		// var newMap : Array[Array[Int]] = new Array[Array[Int]](width * height)
		var newMap: Array[Array[Int]] = Array.ofDim[Int](width, height)
		var collisions: Array[Array[Int]] = Array.fill[Array[Int]](width)(Array.fill[Int](height)(0))
		// val name = System.currentTimeMillis() + ".json"; // this should prolly be the actual seed value instead
		val name = "map" + id + ".json"

		// println("saving " + width + "x" + height +" map to "+name);

		for(i <- 0 until width) {
			for(j <- 0 until height) {
				// if(map(i) < 150 &&  map(i) > 0) {
				if(map(i)(j) == 0) {
					newMap(i)(j) = 121		//light water
					collisions(i)(j) = 1
				}
				// else if(map(i) == 0) {
				else if(map(i)(j) == 1) {
					newMap(i)(j) = 244;	//grass
				}
				else {
					newMap(i)(j) = 241;	//trees
					collisions(i)(j) = 1
				}
			}
		}

    val transports: ListBuffer[JTransport] = ListBuffer()
    transports += new JTransport(width-1, 5, width, min(10, height), id+1, 100, 100)
    transports += new JTransport(0, 5, 1, min(10, height), id-1, width*32-100, 100)

		for(transport <- transports) {
			for(i <- transport.start_x until transport.end_x) {
				for(j <- transport.start_y until transport.end_y) {
					newMap(j)(i) = 347
				}
			}
		}

		val list = newMap.flatten.toList

		case class Layer(map: List[Int], name: String, visible: Boolean)
		// case class Tileset()

		val layers: ListBuffer[Layer] = ListBuffer()
		layers += new Layer(newMap.flatten.toList, "Tile Layer 1", true)
		layers += new Layer(collisions.flatten.toList, "collision", false)
		// val tilesets = ListBuffer(Tileset())

		val tilesets: ListBuffer[Tileset] = ListBuffer()
    tilesets += new Tileset("overworld (1).png", "overworld (1)", 512, 960)


		val json = (
				("id" -> id) ~
				("height" -> height) ~
				("width" -> width) ~
				("layers" -> layers.map { layer => (
					("height" -> height) ~
					("data" -> layer.map) ~
					("name" -> layer.name) ~
					("opacity" -> 1) ~
					("type" -> "tilelayer") ~
					("visible" -> layer.visible) ~
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
				("transports" -> (transports.toList map (_.asJson)))
				// ("transports" -> List[Int]())
			)

		var fw = new FileWriter(new File("src/main/resources/assets/maps/" + name))
		var bw = new BufferedWriter(fw)

		bw.write(pretty(render(json)))
		bw.close()
		fw.close()

		return name

	}
}