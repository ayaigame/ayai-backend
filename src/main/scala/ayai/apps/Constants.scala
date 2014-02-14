package ayai.apps

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import java.rmi.server.UID

object Constants {
  val PLAYER_CHARACTER : String = "player char"
  val STARTING_ROOM_ID : Int = 0

  val source = scala.io.Source.fromFile("src/main/resources/configs/config.json")
  val lines = source.mkString
  source.close()

  val configJSON = parse(lines)
  val NETWORK_TIMEOUT:Int = compact(render(configJSON \ "NETWORK_TIMEOUT")).toInt
  val LOAD_RADIUS:Int = compact(render(configJSON \ "LOAD_RADIUS")).toInt
  val SERVER_PORT:Int = compact(render(configJSON \ "SERVER_PORT")).toInt
  val FRAMES_PER_SECOND = compact(render(configJSON \ "FRAMES_PER_SECOND")).toInt
  val STARTING_X: Int = compact(render(configJSON \ "STARTING_X")).toInt
  val STARTING_Y: Int = compact(render(configJSON \ "STARTING_Y")).toInt
}
