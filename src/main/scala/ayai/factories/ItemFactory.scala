package ayai.factories

/** Ayai Imports **/
import ayai.components._

/** Crane Imports **/
import crane.World
import crane.Entity

/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

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
      stats: Option[List[Stat]],
      image : Option[String])

  def bootup(world: World) = {
    val items: List[AllItemValues] = getItemsList("src/main/resources/configs/items/items.json")

    instantiateWeapons(world, items.filter((item: AllItemValues) => item.itemType == "weapon"))
    // instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "armor"))
    instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "helmet"))
    instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "braces"))
    instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "legs"))
    instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "feet"))

  }

  def buildStats(item: AllItemValues): Stats = {
    var statsArray = new ArrayBuffer[Stat]()
    statsArray.appendAll(item.stats.get)
    new Stats(statsArray)
  }

  def instantiateWeapons(world: World, items: List[AllItemValues]) = {
    items.foreach (item => {
      var entityItem: Entity = world.createEntity()
      var weapon = new Item(
        item.name,
        item.value,
        item.weight,
        new Weapon(item.range.get,
        item.damage.get,
        item.damageType.get, item.itemType))
      weapon.image = item.image.get

      entityItem.components += weapon

      //Construct stats component
      entityItem.components += buildStats(item)
      world.addEntity(entityItem)

      entityItem.tag = "ITEMS" + item.id
    })
  }

  def instantiateArmor(world: World, items: List[AllItemValues]) = {
    items.foreach (item => {
      var entityItem: Entity = world.createEntity()
      var armor = new Item(
        item.name,
        item.value,
        item.weight,
        new Armor(item.slot.get,
        item.protection.get, item.itemType))
      armor.image = ""

      entityItem.components += armor

      //Construct stats component
      entityItem.components += buildStats(item)

      world.addEntity(entityItem)
      entityItem.tag = "ITEMS" + item.id
    })
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
    val otherPaths = (parsedJson \\ "externalItems").extract[List[String]]

    val listOfLists: List[List[AllItemValues]] = otherPaths.map((path: String) => getItemsList(path))
    
    var itemsList = new ArrayBuffer[AllItemValues]()
    itemsList.appendAll(rootItems)

    listOfLists.foreach(e => itemsList.appendAll(e))
    itemsList.toList
  }
}
