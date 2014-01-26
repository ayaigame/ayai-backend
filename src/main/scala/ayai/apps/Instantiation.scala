package ayai.apps

import com.artemis.World
import com.artemis.Entity

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable._

object Instantiation {

  case class AllItemValues(
      id: Int,
      name: String,
      equipable: Boolean,
      itemType: String,
      value: Int,
      weight: Double,
      range: Option[Int],
      damage: Option[Int],
      damageType: Option[String],
      slot: Option[String],
      protection: Option[Int])

  def bootup(world: World) = {
    val items = getItemsList("src/main/resources/configs/items/items.json")
    println(items(0).toString())
  }

  def getItemsList(path: String): List[AllItemValues] = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val source = scala.io.Source.fromFile(path)
    val lines = source.mkString
    source.close()

    val parsedJson = parse(lines)

    val rootItems = (parsedJson \\ "items").extract[List[AllItemValues]]
    val otherPaths = (parsedJson \\ "external_items").extract[List[String]]

    val listOfLists: List[List[AllItemValues]] = otherPaths.map((path: String) => getItemsList(path))
    
    var itemsList = new ArrayBuffer[AllItemValues]()
    itemsList.appendAll(rootItems)

    listOfLists.foreach(e => itemsList.appendAll(e))
    itemsList.toList
  }
}
