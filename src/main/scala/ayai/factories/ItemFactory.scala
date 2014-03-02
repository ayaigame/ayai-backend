package ayai.factories

/** Ayai Imports **/
import ayai.components._
import ayai.gamestate._

/** Crane Imports **/
import crane.World
import crane.Entity

/** External Imports **/
import scala.collection.mutable._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.actor.Status.{Success, Failure}

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
      image: Option[String])

  def bootup(networkSystem: ActorSystem) = {
    val items: List[AllItemValues] = getItemsList("src/main/resources/configs/items/items.json")

    instantiateWeapons(networkSystem, items.filter((item: AllItemValues) => item.itemType == "weapon"))
    // instantiateArmor(world, items.filter((item: AllItemValues) => item.itemType == "armor"))
    instantiateArmor(networkSystem, items.filter((item: AllItemValues) => item.itemType == "helmet"))
    instantiateArmor(networkSystem, items.filter((item: AllItemValues) => item.itemType == "braces"))
    instantiateArmor(networkSystem, items.filter((item: AllItemValues) => item.itemType == "legs"))
    instantiateArmor(networkSystem, items.filter((item: AllItemValues) => item.itemType == "feet"))
  }

  def buildStats(item: AllItemValues): Stats = {
    var statsArray = new ArrayBuffer[Stat]()
    statsArray.appendAll(item.stats.get)
    new Stats(statsArray)
  }

  def instantiateWeapons(networkSystem: ActorSystem, items: List[AllItemValues]) = {
    items.foreach (item => {
      var weapon = new Item(
        item.name,
        item.value,
        item.weight,
        new Weapon(item.range.get,
        item.damage.get,
        item.damageType.get, item.itemType))
      weapon.image = item.image.get

      networkSystem.actorSelection("user/ItemMap") ! AddItem("ITEM"+item.id, weapon)
    })
  }

  def instantiateArmor(networkSystem: ActorSystem, items: List[AllItemValues]) = {
    items.foreach (item => {
      var armor = new Item(
        item.name,
        item.value,
        item.weight,
        new Armor(item.slot.get,
        item.protection.get, item.itemType))
      armor.image = ""

      networkSystem.actorSelection("user/ItemMap") ! AddItem("ITEM"+item.id, armor)
    })
  }

  def instantiateConsumables(networkSystem: ActorSystem, items: List[AllItemValues]) = {

  }

  def instantiateBaseItems(networkSystem: ActorSystem,  items: List[AllItemValues]) = {

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
