package ayai.persistence

import ayai.components.{Character, Position, Room, Inventory, Item}
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
  def upsertItem(inventoryRow: InventoryRow) {
    val itemQuery =
      from(AyaiDB.inventory)(row =>
        where(row.playerId === inventoryRow.playerId
          and row.itemId === inventoryRow.itemId)
        select(row)
      )

    if(itemQuery.size == 0) {
      AyaiDB.inventory.insert(inventoryRow)
    }
    else if (itemQuery.size == 1) {
      AyaiDB.inventory.update(inventoryRow)
    }
    else {
      println("Error: duplicate inventory row.")
    }
  }

  //MUST BE CALLED FROM WITHIN TRANSACTION
  //This is so it can be done multiple times within a single transaction.
  //Deletes all copies of the item in the character's inventory
  def deleteAllOfItem(itemEnity: Entity, characterEntity: Entity) {
   (entity.getComponent(classOf[Item]),
      entity.getComponent(classOf[Character])) match {
      case (Some(item: Item), Some(character: Character)) =>
        // val itemQuery =
        //   from(AyaiDB.inventory)(row =>
        //     where(row.playerId === character.id
        //       and row.itemId === item.id)
        //     select(row)
        //   )

        // if(itemQuery.size > 0) {
          AyaiDB.inventory.deleteWhere(row =>
            (row.playerId === character.id) and
            (row.itemId === item.id)
          )
        // }
  }

  //MUST BE CALLED FROM WITHIN TRANSACTION
  //This is so it can be done multiple times within a single transaction.
  def deleteItem(itemEnity: Entity, characterEntity: Entity) {
   (entity.getComponent(classOf[Inventory]),
      entity.getComponent(classOf[Character])) match {
      case (Some(item: Item), Some(character: Character)) =>
        val itemQuery =
          from(AyaiDB.inventory)(row =>
            where(row.playerId === character.id
              and row.itemId === item.id)
            select(row)
          )

        if (itemQuery.size == 1) {
          newRow = itemQuery.single
          newRow.quantity = newRow.quantity - 1
          if(newRow.quantity > 0)
            AyaiDB.inventory.update(newRow)
          else {
            AyaiDB.inventory.deleteWhere(row =>
              (row.playerId === character.id) and
              (row.itemId === item.id)
            )
          }
        }
        else {
          println("Error: duplicate inventory row.")
        }
      case _ =>
        println("Error in InventoryTable")
    }
  }

  def saveInventory(entity: Entity) {
    (entity.getComponent(classOf[Inventory]),
      entity.getComponent(classOf[Character])) match {
      case (Some(inventory: Inventory), Some(character: Character)) =>
        AyaiDB.getCharacter(character.name) match {
          case Some(characterRow: CharacterRow) =>
            Class.forName("org.h2.Driver");
            SessionFactory.concreteFactory = Some (() =>
                Session.create(
                java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
                new H2Adapter))

            val inventoryRows = inventory.inventory groupBy ((item:Item) => item.id) map {p => new InventoryRow(characterRow.id, p._1, p._2.length)}
            println(s"Inserting $inventoryRows")

            transaction {
              inventoryRows map upsertItem
            }
          case _ =>
            println(s"Can't find account for $character.name")
        }
      case _ =>
        println("Can't get inventory table for some reason.")
    }
  }

  // def saveItem(itemEnity: Entity, characterEntity: Entity) = {
  // (entity.getComponent(classOf[Position]),
  //   entity.getComponent(classOf[Character]),
  //   entity.getComponent(classOf[Room])) match {
  //     case(Some(position : Position), Some(character : Character), Some(room : Room)) =>
  //       Class.forName("org.h2.Driver");
  //       SessionFactory.concreteFactory = Some (() =>
  //           Session.create(
  //           java.sql.DriverManager.getConnection("jdbc:h2:ayai"),
  //           new H2Adapter))

  //       transaction {
  //         update(AyaiDB.characters)(dbCharacter =>
  //           where(dbCharacter.name === character.name)
  //           set(dbCharacter.experience := character.experience,
  //               dbCharacter.pos_x := position.x,
  //               dbCharacter.pos_y := position.y,
  //               dbCharacter.room_id := room.id))
  //       }
  //     case _ =>
  //       println("Can't get inventory table for some reason.")
  //   }
  // }
}