package globalvariables

import java.nio.ByteBuffer

import boopickle.CompositePickler
import boopickle.Default._
import sharednodejsapis.{Buffer, IPCRenderer}

import scala.scalajs.js
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.scalajs.js.JSConverters._


abstract sealed class Data

/**
 * The DataStorage object allows to store [[Data]] to the main process.
 * This is much more type safe than VariableStorage, which will probably be deleted in the future.
 */
object DataStorage {

  private implicit val dataStoragePickler: CompositePickler[Data] = {
    compositePickler[Data]
      .addConcreteType[BaseDirectory]
      .addConcreteType[WindowId]

      .addConcreteType[GameData]
      .addConcreteType[PlayerData]
      .addConcreteType[PlayerDataList]
      .addConcreteType[EndOfGameData]
      .addConcreteType[LifeTimeStamp]
      .addConcreteType[Color]
      .addConcreteType[StandardModeEOGData]
      .addConcreteType[CaptureTheFlagModeEOGData]

      .addConcreteType[PlayerStat]
      .addConcreteType[PlayerStats]
  }

  def storeValue(key: String, data: Data): Unit = {
//    val serialized = Pickle.intoBytes(data)
//    val buffer = Buffer.from(serialized.arrayBuffer())
//    IPCRenderer.sendSync("store-value", key, buffer.toJSArray)
    IPCRenderer.sendSync("store-value", key, encode(data))
  }


  def retrieveValue(key: String): Data = {
//    val buffer = IPCRenderer.sendSync("retrieve-value", key).asInstanceOf[js.Array[Int]].map(_.toByte)
//    Unpickle[Data](dataStoragePickler).fromBytes(ByteBuffer.wrap(buffer.toArray))
    decode(IPCRenderer.sendSync("retrieve-value", key).asInstanceOf[js.Array[Byte]])
  }

  def unStoreValue(key: String): Unit = {
    IPCRenderer.sendSync("unStore-value", key)
  }

  def retrieveGlobalValue(key: String): Data = {
    decode(IPCRenderer.sendSync("retrieve-global-value", key).asInstanceOf[js.Array[Byte]])
  }

  def storeGlobalValue(key: String, data: Data): Unit = {
    IPCRenderer.sendSync("store-global-value", key, encode(data))
  }

  def decode(buffer: scala.scalajs.js.Array[Byte]): Data =
    Unpickle[Data](dataStoragePickler).fromBytes(ByteBuffer.wrap(buffer.toArray))

  def encode(data: Data): scala.scalajs.js.Array[Byte] =
    Buffer.from(Pickle.intoBytes(data).arrayBuffer()).toJSArray.map(_.toByte)


}

final case class BaseDirectory(directory: String) extends Data
final case class WindowId(id: Int) extends Data

final case class GameData(gameName: String,
                          playerName: String,
                          reservationId: Int,
                          address: String,
                          port: Int,
                          host: Boolean,
                          gameMode: String) extends Data

final case class PlayerData(gameName: String,
                            playerName: String,
                            id: Long,
                            team: Int,
                            ready: Boolean,
                            abilities: List[Int]) extends Data

final case class PlayerDataList(data: List[PlayerData]) extends Data

sealed trait EndOfGameData extends Data
final case class StandardModeEOGData(playersFromLastToFirstDeath: List[String]) extends EndOfGameData
final case class CaptureTheFlagModeEOGData(scores: Map[Int, Int]) extends EndOfGameData

final case class LifeTimeStamp(time: Long, life: Double) extends EndOfGameData

final case class Color(red: Double, green: Double, blue: Double) extends EndOfGameData

final case class PlayerStat(
                             id: Long,
                             playerName: String,
                             color: Color,
                             teamId: Int,
                             ability: String,
                             sentBulletsTimes: List[Long],
                             sentBullets: List[Long],
                             bulletHits: Int,
                             bulletHitsTimes: List[Long],
                             damageTaken: Double,
                             bulletHitPlayerNbr: Int,
                             takenHealUnits: Int,
                             totalMovement: Double,
                             deathTime: Option[Long],
                             lifeOverTime: List[LifeTimeStamp]
                           ) extends EndOfGameData

final case class PlayerStats(
                              playerName: String,
                              gameDuration: Long,
                              startTime: Long,
                              stats: List[PlayerStat]
                            ) extends EndOfGameData
