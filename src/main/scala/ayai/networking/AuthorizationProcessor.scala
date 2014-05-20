package ayai.networking

import ayai.persistence._
import ayai.factories.ClassFactory
import ayai.factories.NPCFactory
import ayai.statuseffects._
import ayai.components._
import ayai.gamestate._
import ayai.apps.Constants
/** Akka Imports **/
import akka.actor.{Actor}

/** Socko Imports **/
import org.mashupbots.socko.events.{HttpRequestEvent, HttpResponseStatus}
import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.concurrent.duration._

import akka.actor.{Actor, ActorSystem, ActorRef, Props, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class AuthorizationProcessor(networkSystem: ActorSystem) extends Actor {
  implicit val timeout = Timeout(Constants.NETWORK_TIMEOUT seconds)
  Class.forName("org.h2.Driver");
    SessionFactory.concreteFactory = Some (() =>
      Session.create(
      java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
      new H2Adapter))

  def receive = {
  case LoginPost(request: HttpRequestEvent) =>
    val headers = request.request.headers
    headers.get("Authorization") match {
    case Some(basicAuth: String) =>
      val auth = new String(new sun.misc.BASE64Decoder().decodeBuffer(basicAuth.replaceAll("Basic ","")))
      val delimiter = auth.indexOfSlice(":")
      val username = auth.slice(0, delimiter)
      val password = auth.slice(delimiter + 1, auth.length)

      // For signing cookies later
      val conf = ConfigFactory.load
      val serverConf = conf.getConfig("server")
      lazy val secret: String = serverConf.getString("secret")

      var token: String = ""

      token = AccountTable.validatePassword(username, password)

      token match {
      case "" =>
        request.response.write(HttpResponseStatus.UNAUTHORIZED)
      case _ =>
        request.response.write(HttpResponseStatus.OK, token)
      }
    case _ =>
      request.response.write(HttpResponseStatus.UNAUTHORIZED)

    }

  case RegisterPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")

    val username = content.slice(0, delimiter).replaceAll("email=", "")
    val password = content.slice(delimiter + 1, content.length).replaceAll("password=", "")
    if(AccountTable.registerUser(username, password)) {
      var token: String = ""
      token = AccountTable.validatePassword(username, password)

      token match {
        case "" =>
          request.response.write(HttpResponseStatus.UNAUTHORIZED)
        case _ =>
          request.response.write(HttpResponseStatus.OK, token)
      }
    }
    else
      request.response.write(HttpResponseStatus.CONFLICT)

  case CharactersPost(request: HttpRequestEvent) =>
    val token: String = request.request.content.toString
    var accountId = AccountTable.getAccountIdFromToken(token)

    if(accountId == -1)
      request.response.write(HttpResponseStatus.UNAUTHORIZED)
    else
      CharacterTable.characterList(request, accountId)

  case ClassListGet(request: HttpRequestEvent) =>
    request.response.write(HttpResponseStatus.OK, compact(render(ClassFactory.asJson)))

  case CreateCharacterPost(request: HttpRequestEvent) =>
    val content:String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")

    val userToken = content.slice(0, delimiter).replaceAll("token=", "")
    val characterName = content.slice(delimiter + 1, delimiter2).replaceAll("name=", "")
    val className = content.slice(delimiter2 + 1, content.length).replaceAll("class=", "")

    var accountId = AccountTable.getAccountIdFromToken(userToken)

    if(accountId == -1)
      request.response.write(HttpResponseStatus.UNAUTHORIZED)
    else
      CharacterTable.createCharacter(request, characterName, className, accountId)

  case RecoveryPost(request: HttpRequestEvent) =>
    println("RECOVERY")

  case ItemPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    println(content)
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(0).replaceAll("id=", "")
    val name = contentSplit(1).replaceAll("name=","")
    val description = contentSplit(2).replaceAll("description=","")
    val value = contentSplit(3).replaceAll("value=","").toInt
    val weight = contentSplit(4).replaceAll("weight=","").toDouble
    val imageLocation = contentSplit(5).replaceAll("image=","")
    val itemType = contentSplit(6).replaceAll("itemType=","")

    println("ItemType: Found" + itemType)

    var item: Item = null

    itemType.toLowerCase match {
      case "weapon1" => 
        println("Inside weapon1")
        val range = contentSplit(7).replaceAll("range=","").toInt
        val damage = contentSplit(8).replaceAll("damage=","").toInt
        val damageType = contentSplit(9).replaceAll("damageType=","")
        val itemT = "weapon1"
        item = new Item(id.toLong, name, value, weight, new Weapon(range, damage, damageType, itemT))
        networkSystem.actorSelection("user/ItemMap") ! AddItem(id, item)
        request.response.write(HttpResponseStatus.OK)
      case "armor" =>
        val slot = contentSplit(10).replaceAll("slot=","")
        val protection = contentSplit(11).replaceAll("protection=","").toInt
        val itemT = contentSplit(12).replaceAll("armorType=","")
        item = new Item(id.toLong, name, value, weight, new Armor(slot, protection, itemT))
        networkSystem.actorSelection("user/ItemMap") ! AddItem(id, item)
        request.response.write(HttpResponseStatus.OK)
      case "consumable" => 
        item = new Item(id.toLong, name, value, weight, new Consumable())
        networkSystem.actorSelection("user/ItemMap") ! AddItem(id, item)
        request.response.write(HttpResponseStatus.OK)

      case _ =>
        request.response.write(HttpResponseStatus.UNAUTHORIZED)
    }

  case NPCGet(request: HttpRequestEvent) => 
    val jsonFuture = networkSystem.actorSelection("user/NPCMap") ? OutputJson()
    val json = Await.result(jsonFuture, timeout.duration).asInstanceOf[String]
    request.response.write(HttpResponseStatus.OK, json)

  case EffectGet(request: HttpRequestEvent) => 
    val jsonFuture = networkSystem.actorSelection("user/EffectMap") ? OutputJson()
    val json = Await.result(jsonFuture, timeout.duration).asInstanceOf[String]
    request.response.write(HttpResponseStatus.OK, json)

  case ItemGet(request: HttpRequestEvent) => 
    val jsonFuture = networkSystem.actorSelection("user/ItemMap") ? OutputJson()
    val json = Await.result(jsonFuture, timeout.duration).asInstanceOf[String]
    request.response.write(HttpResponseStatus.OK, json)

  case ClassGet(request: HttpRequestEvent) => 
    val jsonFuture = networkSystem.actorSelection("user/ClassMap") ? OutputJson()
    val json = Await.result(jsonFuture, timeout.duration).asInstanceOf[String]
    request.response.write(HttpResponseStatus.OK, json)


  case NPCPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val userToken = content.slice(0, delimiter).replaceAll("token=","")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(1).replaceAll("id=", "")
    val name = contentSplit(2).replaceAll("name=","")
    val faction = contentSplit(3).replaceAll("faction=","")
    val room = contentSplit(4).replaceAll("room=","").toInt
    val weapon1 = contentSplit(5).replaceAll("weapon1=","").toInt
    val helmet = contentSplit(6).replaceAll("helmet=","").toInt
    val torso = contentSplit(7).replaceAll("torso=","").toInt
    val legs = contentSplit(8).replaceAll("legs=","").toInt
    val feet = contentSplit(9).replaceAll("feet=","").toInt
    val level = contentSplit(10).replaceAll("level=","").toInt
    val experience = contentSplit(11).replaceAll("experience=","").toInt
    val health = contentSplit(12).replaceAll("health=","").toInt
    val mana = contentSplit(13).replaceAll("mana=","").toInt

    //get equipment ids for weapons given
    // val weaponFuture = networkSystem.actorSelection("user/ItemMap") ? GetItem(weapon1)
    // val weaponItem = Await.result(weaponFuture, timeout.duration).asInstanceOf[Item]
    // val legsFuture = networkSystem.actorSelection("user/ItemMap") ? GetItem(legs)
    // val legsItem = Await.result(legsFuture, timeout.duration).asInstanceOf[Item]
    // val torsoFuture = networkSystem.actorSelection("user/ItemMap") ? GetItem(torso)
    // val torsoItem = Await.result(torsoFuture, timeout.duration).asInstanceOf[Item]
    // val helmetFuture = networkSystem.actorSelection("user/ItemMap") ? GetItem(helmet)
    // val helmetItem = Await.result(helmetFuture, timeout.duration).asInstanceOf[Item]
    // val feetFuture = networkSystem.actorSelection("user/ItemMap") ? GetItem(feet)
    // val feetItem = Await.result(feetFuture, timeout.duration).asInstanceOf[Item]

    // val equipment: Equipment = new Equipment()
    // equipment.equipItem(weaponItem)
    // equipment.equipItem(feetItem)
    // equipment.equipItem(legsItem)
    // equipment.equipItem(helmetItem)
    // equipment.equipItem(torsoItem)
    networkSystem.actorSelection("user/NPCMap") ! AddNPC(id, new NPCValues(id.toInt, name, faction,
      room, weapon1, torso, legs, helmet, feet , level, experience, health, mana))

    
    request.response.write(HttpResponseStatus.OK)

  case ClassPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    println(content)
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val userToken = content.slice(0, delimiter).replaceAll("token=","")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(0).replaceAll("id=", "")
    val name = contentSplit(1).replaceAll("name=","").toLowerCase
    val description = contentSplit(2).replaceAll("description=","")
    val baseHealth = contentSplit(3).replaceAll("health=","").toInt
    val baseMana = contentSplit(4).replaceAll("mana=","").toInt
    val spriteSheetLocation = contentSplit(5).replaceAll("spritesheet=","")
    val statsString = contentSplit(6).replaceAll("stats=","")
    val baseStats = contentSplit(7).replaceAll("base=","")
    val growthStats = contentSplit(8).replaceAll("growth=","")
    
    val statsContent = statsString.split("%2C")
    val baseContent = baseStats.split("%2C")
    val growthContent = growthStats.split("%2C")

    val stats = new Stats()
    for(a <- 0 until statsContent.length) {
      stats.addStat(new Stat(statsContent(a), baseContent(a).toInt, growthContent(a).toInt))
    }

    val classValue = new ClassValues(id, name, description,  baseHealth, baseMana, stats)
        
    networkSystem.actorSelection("user/ClassMap") ! AddClass(id, classValue)

    request.response.write(HttpResponseStatus.OK)

  case EffectPost(request: HttpRequestEvent) =>
    val content: String = request.request.content.toString
    val delimiter = content.indexOfSlice("&")
    val delimiter2 = content.lastIndexOfSlice("&")
    val contentSplit = content.split("&")
    //get item information
    val id = contentSplit(0).replaceAll("id=", "")
    val name = contentSplit(1).replaceAll("name=","")
    val description = contentSplit(2).replaceAll("description=","")
    val effectType = contentSplit(3).replaceAll("effectType=","")
    val value:Int = contentSplit(4).replaceAll("value=","").toInt
    val attribute = contentSplit(5).replaceAll("attribute=","")

    
    //get required info needed for these 
    attribute.toLowerCase() match {
      case "oneoff" =>
        val multiplier:Double = contentSplit(7).replaceAll("multiplier=","").toDouble
        val isRelative:Boolean = contentSplit(8).replaceAll("isRelative=","").toBoolean
        val isValueRelative:Boolean = contentSplit(9).replaceAll("isValueRelative=","").toBoolean
        val image = contentSplit(10).replaceAll("imageLocation=","")      
        val attributeType: TimeAttribute = new OneOff()
        val effect = new Effect(id.toInt, name, 
                            description, effectType,
                            value, attributeType,
                            new Multiplier(multiplier), isRelative, isValueRelative)
        networkSystem.actorSelection("user/EffectMap") ! AddEffect(id, effect)
        request.response.write(HttpResponseStatus.OK)

      case "duration" =>
        val duration:Int = contentSplit(7).replaceAll("length=","").toInt
        val multiplier:Double = contentSplit(8).replaceAll("multiplier=","").toDouble
        val isRelative:Boolean = contentSplit(9).replaceAll("isRelative=","").toBoolean
        val isValueRelative:Boolean = contentSplit(10).replaceAll("isValueRelative=","").toBoolean
        val image = contentSplit(11).replaceAll("imageLocation=","")
        val attributeType: TimeAttribute = new ayai.statuseffects.Duration(duration)
        val effect = new Effect(id.toInt, name, 
                            description, effectType,
                            value, attributeType,
                            new Multiplier(multiplier), isRelative, 
                            isValueRelative)
        networkSystem.actorSelection("user/EffectMap") ! AddEffect(id, effect)
        request.response.write(HttpResponseStatus.OK)
      case "timedinterval" =>
        val duration:Int = contentSplit(7).replaceAll("length=","").toInt
        val interval:Int = contentSplit(8).replaceAll("interval=","").toInt
        val multiplier:Double = contentSplit(9).replaceAll("multiplier=","").toDouble
        val isRelative:Boolean = contentSplit(10).replaceAll("isRelative=","").toBoolean
        val isValueRelative:Boolean = contentSplit(11).replaceAll("isValueRelative=","").toBoolean
        val image = contentSplit(12).replaceAll("imageLocation=","")
        val attributeType: TimeAttribute = new TimedInterval(duration, interval)
        val effect = new Effect(id.toInt,name, 
                            description, effectType,
                            value, attributeType,
                            new Multiplier(multiplier), isRelative, isValueRelative )
        networkSystem.actorSelection("user/EffectMap") ! AddEffect(id, effect)
        request.response.write(HttpResponseStatus.OK)
      case _ => request.response.write(HttpResponseStatus.UNAUTHORIZED)
    }   
  case _ => println("Error from AuthorizationProcessor.")
  }
}
