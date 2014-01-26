package ayai.apps

import ayai.components._

import com.artemis.World
import com.artemis.Entity
import com.artemis.managers.TagManager

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.collection.mutable._

object ItemFactory {

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
      protection: Option[Int],
      stackable: Option[Boolean],
      stats: Option[Stats])

  def bootup(world: World) = {
    val items: List[AllItemValues] = getItemsList("src/main/resources/configs/items/items.json")

    instantiateWeapons(world, items.filter((item: AllItemValues) => item.itemType == "weapon"))
    instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "armor"))
  }

  def instantiateWeapons(world: World, items: List[AllItemValues]) = {
    items.foreach (item => {
      var entityItem: Entity = world.createEntity()
      var weapon: Weapon = new Weapon(
        item.name,
        item.value,
        item.weight,
        item.range.getOrElse(-1),
        item.damage.getOrElse(-1),
        item.damageType.getOrElse(""))

      entityItem.addComponent(weapon)

      entityItem.addToWorld
      world.getManager(classOf[TagManager]).register("ITEMS" + item.id, entityItem)
    })
  }

  def instantiateArmor(world: World, items: List[AllItemValues]) = {
    // println("Armor: " + items.toString())
  }

  def instantiateConsumables(world: World, items: List[AllItemValues]) = {

  }

  def instantiateBaseItems(world: World, items: List[AllItemValues]) = {

  }

  def addStats(item: Entity, stats: Stats) = {

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
