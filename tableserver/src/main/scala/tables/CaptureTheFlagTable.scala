package tables
import gamemode.{CaptureTheFlagMode, GameMode}
import networkcom.Server
import networkcom.tablemessages.PlayerInfoMessage

import scala.collection.mutable

final class CaptureTheFlagTable(
                                  val host: String,
                                  val name: String,
                                  val password: Option[String],
                                  val server: Server
                               ) extends Table {

  val gameMode: GameMode = CaptureTheFlagMode

  type T = CaptureTheFlagTable.PlayerInfo

  protected val playersInfo: mutable.Map[String, CaptureTheFlagTable.PlayerInfo] = mutable.Map()

  def receivedPlayerInfo(playerName: String, ready: Boolean, abilities: List[Int], team: Int): Unit = {
    if (isPlayerSeated(playerName)) {
      playersInfo += playerName -> CaptureTheFlagTable.PlayerInfo(
        playerName, team, abilities, ready
      )

      broadcastTableInfo()
    }
  }

  def sendTableInfo(playerName: String, server: Server): Unit = ???

  def newInfo(playerName: String): CaptureTheFlagTable.PlayerInfo =
    CaptureTheFlagTable.PlayerInfo(playerName, 1, List(), ready = false)

  def receivedPlayerInfo(message: PlayerInfoMessage): Unit = ???

}

object CaptureTheFlagTable {

  def apply(host: String, name: String, password: Option[String], server: Server): CaptureTheFlagTable = {
    new CaptureTheFlagTable(host, name, password, server)
  }


  final case class PlayerInfo(
                               playerName: String,
                               team: Int,
                               abilities: List[Int],
                               ready: Boolean
                             ) extends Table.PlayerInfo

}