package ayai.apps

/** External Imports **/
import net.liftweb.json._

object Constants {
  implicit val formats = net.liftweb.json.DefaultFormats

  val PLAYER_CHARACTER: String = "player char"
  val STARTING_ROOM_ID: Int = 0
  val PROJECTILE_VELOCITY = 8

  val source = scala.io.Source.fromFile("src/main/resources/configs/config.json")
  val lines = source.mkString
  source.close()

  val configJSON = parse(lines)
  val NETWORK_TIMEOUT = extractValue[Int]("NETWORK_TIMEOUT")
  val LOAD_RADIUS = extractValue[Int]("LOAD_RADIUS")
  val SERVER_PORT = extractValue[Int]("SERVER_PORT")
  val FRAMES_PER_SECOND = extractValue[Int]("FRAMES_PER_SECOND")
  val STARTING_X = extractValue[Int]("STARTING_X")
  val STARTING_Y = extractValue[Int]("STARTING_Y")

  val experienceSource = scala.io.Source.fromFile("src/main/resources/configs/classes/experience.json")
  val experienceLines = experienceSource.mkString
  val SPACE_FOR_INTERACTION = 140
  experienceSource.close()

  val EXPERIENCE_ARRAY = parse(experienceLines).extract[List[Int]]

  private def extractValue[T](key: String)(implicit mf: Manifest[T]): T = {
    (configJSON \ key).extract[T]
  }
}
