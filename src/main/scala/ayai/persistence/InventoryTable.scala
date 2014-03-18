package ayai.persistence

import ayai.components.{Character, Inventory, Item, Equipment, Weapon, Armor}
import ayai.apps.Constants

import scala.collection.mutable.ArrayBuffer

import crane.Entity

/** External Imports **/
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._
import org.mindrot.jbcrypt.BCrypt

/** Socko Imports **/
import org.mashupbots.socko.events.{HttpRequestEvent, HttpResponseStatus}

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

object InventoryTable {
  //MUST BE CALLED FROM WITHIN TRANSACTION
  //This is so it can be done multiple times within a single transaction.
  //Deletes all copies of the item in the character's inventory
  def deleteAllOfItem(itemEnity: Entity, characterEntity: Entity) {
   (itemEnity.getComponent(classOf[Item]),
      characterEntity.getComponent(classOf[Character])) match {
      case (Some(item: Item), Some(character: Character)) =>
        val characterQuery =
          from(AyaiDB.characters)(row =>
            where(row.name === character.name)
            select(row)
          )

        AyaiDB.inventory.deleteWhere(row =>
          (row.characterId === characterQuery.single.id) and
          (row.itemId === item.id)
        )

      case _ =>
        println("Error in InventoryTable - deleteAllOfItem")
      }
  }

  //MUST BE CALLED FROM WITHIN TRANSACTION
  //This is so it can be done multiple times within a single transaction.
  def deleteItem(itemEnity: Entity, characterEntity: Entity) {
   (itemEnity.getComponent(classOf[Item]),
      characterEntity.getComponent(classOf[Character])) match {
      case (Some(item: Item), Some(character: Character)) =>
        val characterQuery =
            from(AyaiDB.characters)(row =>
              where(row.name === character.name)
              select(row)
            )

        val itemQuery =
          from(AyaiDB.inventory)(row =>
            where(row.characterId === characterQuery.single.id
              and row.itemId === item.id)
            select(row)
          )

        if (itemQuery.size == 1) {
          var inventoryRow = itemQuery.single
          if(inventoryRow.quantity > 0) {
            var newRow = new InventoryRow(inventoryRow.characterId, inventoryRow.itemId, inventoryRow.quantity - 1)
            AyaiDB.inventory.update(newRow)
          }
          else {
            AyaiDB.inventory.deleteWhere(row =>
              (row.characterId === inventoryRow.characterId) and
              (row.itemId === item.id)
            )
          }
        }
        else {
          println("Error: duplicate inventory row.")
        }
      case _ =>
        println("Error in InventoryTable - deleteItem")
    }
  }

  //Deletes all item rows for the character out of both InventoryTable and EquipmentTable.
  //Then saves all items from Inventory to InventoryTable and Equipment to EquipmentTable.
  def saveInventory(entity: Entity) {
    (entity.getComponent(classOf[Inventory]),
      entity.getComponent(classOf[Character]),
      entity.getComponent(classOf[Equipment])) match {
      case (Some(inventory: Inventory), Some(character: Character), Some(equipment: Equipment)) =>
        AyaiDB.getCharacter(character.name) match {
          case Some(characterRow: CharacterRow) =>
            Class.forName("org.h2.Driver");
            SessionFactory.concreteFactory = Some (() =>
                Session.create(
                java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
                new H2Adapter))

            val equipmentList: List[Item] = equipment.equipmentMap.values.toList

            //delete all items
            transaction {
              AyaiDB.equipment.deleteWhere(row =>
                (row.characterId === characterRow.id))
              AyaiDB.inventory.deleteWhere(row =>
                (row.characterId === characterRow.id))
            }

            def getSlot(item: Item): String = {
              var slot = ""
              item.itemType match {
                  case weapon: Weapon =>
                    slot = weapon.itemType
                  case armor: Armor =>
                    slot = armor.itemType
                  case _ =>
                    println(s"Error with item $item , cannot save.")
                }
              return slot
            }

            //save equipment
            transaction {
              equipmentList map ((item: Item)=> AyaiDB.equipment.insert(new EquipmentRow(characterRow.id, item.id, getSlot(item))))
            }

            val inventoryRows = inventory.inventory groupBy(
              (item:Item) => item.id) map {
                p => new InventoryRow(characterRow.id, p._1, p._2.length)}

            println(s"Saving $inventoryRows")
            transaction {
              inventoryRows foreach ((row: InventoryRow) => AyaiDB.inventory.insert(row))
            }
          case _ =>
            println(s"Can't find account for $character.name")
        }
      case _ =>
        println("Can't get inventory table for some reason.")
    }
  }

  //Upserts the item.
  //  If it doesn't exist creates it with quantity 1.
  //  If it does exist it will increment the quantity field.
  //Don't use for a lot of saves. Each call will be a seperate transaction.
  def incrementItem(itemEnity: Entity, characterEntity: Entity) {
    incrementItemMultiple(itemEnity, characterEntity, 1)
  }

  //Upserts the item.
  //  If it doesn't exist creates it with quantity=quantity.
  //  If it does exist it will increment the quantity field by the quantity parameter.
  //Don't use this in a loop. Each call will be a seperate transaction.
  def incrementItemMultiple(itemEnity: Entity, characterEntity: Entity, quantity: Int) {
  (itemEnity.getComponent(classOf[Item]),
    characterEntity.getComponent(classOf[Character])) match {
      case(Some(item: Item), Some(character : Character)) =>
        Class.forName("org.h2.Driver");
        SessionFactory.concreteFactory = Some (() =>
            Session.create(
            java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
            new H2Adapter))

        transaction {
          val characterQuery =
            from(AyaiDB.characters)(row =>
              where(row.name === character.name)
              select(row)
            )

          val itemQuery =
            from(AyaiDB.inventory)(row =>
              where(row.characterId === characterQuery.single.id
                and row.itemId === item.id)
              select(row)
            )

          if(itemQuery.size == 0) {
            AyaiDB.inventory.insert(new InventoryRow(characterQuery.single.id, item.id, quantity))
          }
          else if (itemQuery.size == 1) {
            var inventoryRow = itemQuery.single
            val newRow = new InventoryRow(inventoryRow.characterId, inventoryRow.itemId, inventoryRow.quantity + quantity)
            AyaiDB.inventory.update(newRow)
          }
          else {
            println("Error: duplicate inventory row.")
          }
        }

      case _ =>
        println("Can't get inventory table for some reason.")
    }
  }
}
