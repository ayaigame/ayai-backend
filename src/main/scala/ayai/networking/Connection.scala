package ayai.networking

/** Ayai Imports **/
import ayai.actions._

/** Socko Imports **/
import org.mashupbots.socko.events.HttpRequestEvent
import org.mashupbots.socko.events.WebSocketFrameEvent


sealed trait Message
sealed trait ProcessorMessage extends Message
sealed trait EventMessage extends Message
sealed trait NetworkMessage extends Message

case class ProcessMessage(message: Message)
case class FlushMessages(worldId: Int)
case class QueuedMessages(messages: Array[Message])
case class AddInterpretedMessage(worldId: Int, message: Message)
case class InterpretMessage(message: WebSocketFrameEvent)
case class ConnectionWrite(json: String)

case class JSONMessage(message: String) extends ProcessorMessage
case class AddNewCharacter(userId: String, characterName: String, x: Int, y: Int) extends ProcessorMessage
case class RemoveCharacter(id: String) extends ProcessorMessage
case class MoveMessage(socketId: String, start: Boolean, direction: MoveDirection) extends ProcessorMessage
case class ItemMessage(id : String, itemAction : ItemAction) extends ProcessorMessage
case class AttackMessage(socketId: String) extends ProcessorMessage
case class SocketCharacterMap(socketId: String, id: String) extends ProcessorMessage

case class LoginPost(request: HttpRequestEvent) extends NetworkMessage
case class RegisterPost(request: HttpRequestEvent) extends NetworkMessage
case class RecoveryPost(request: HttpRequestEvent) extends NetworkMessage
case class CharactersPost(request: HttpRequestEvent) extends NetworkMessage
case class CreateCharacterPost(request: HttpRequestEvent) extends NetworkMessage
case class ClassListGet(request: HttpRequestEvent) extends NetworkMessage
case class NPCPost(request: HttpRequestEvent) extends NetworkMessage
case class QuestPost(request: HttpRequestEvent) extends NetworkMessage
case class ClassPost(request: HttpRequestEvent) extends NetworkMessage
case class ItemPost(request: HttpRequestEvent) extends NetworkMessage
case class EffectPost(request: HttpRequestEvent) extends NetworkMessage
case class SpellPost(request: HttpRequestEvent) extends NetworkMessage
case class NPCGet(request: HttpRequestEvent) extends NetworkMessage
case class EffectGet(request: HttpRequestEvent) extends NetworkMessage
case class ItemGet(request: HttpRequestEvent) extends NetworkMessage
case class ClassGet(request: HttpRequestEvent) extends NetworkMessage

case class PublicChatMessage(message: String, sender: String) extends NetworkMessage
case class OpenMessage(socketId: String, containerId: String) extends NetworkMessage
case class CharacterList(socketId: String, accountName: String) extends NetworkMessage
case class EquipMessage(userId: String, slot: Int, equipmentType: String) extends NetworkMessage
case class UnequipMessage(userId: String, equipmentType: String) extends NetworkMessage
case class DropItemMessage(userId: String, slot: Int) extends NetworkMessage
case class AbandonQuestMessage(userId: String, questId: Int) extends NetworkMessage
case class DeclineQuestMessage(userId: String, npcId: String, questId: String) extends NetworkMessage
case class AcceptQuestMessage(userId: String, entityID: String, questId: Int) extends NetworkMessage
case class InteractMessage(userId: String, entityId: String) extends NetworkMessage
case class LootMessage(userId: String, entityId: String, itemId: Int) extends NetworkMessage
case class RequestLootInventory(userId: String, entityId: String) extends NetworkMessage
case object CreateAIMessage extends NetworkMessage
case class UseItemMessage(userId: String, itemId: Int) extends NetworkMessage
case class SpawnMessage(userId: String, entityType: String, entityTypeId: Int, x: Int, y: Int) extends NetworkMessage
