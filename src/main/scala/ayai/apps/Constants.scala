package ayai.apps

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object Constants {
  var PLAYER_CHARACTER : String = "player char"

  val source = scala.io.Source.fromFile("src/main/resources/configs/config.json")
  val lines = source.mkString
  source.close()

  val configJSON = parse(lines)
  val NETWORK_TIMEOUT:Int = compact(render(configJSON \ "network_timeout")).toInt


}
