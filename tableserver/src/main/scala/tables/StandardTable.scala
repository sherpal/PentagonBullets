package tables

import exceptions.ShouldNotReceiveThisMessageException
import gamemode.{GameMode, StandardMode}
import networkcom.Server
import networkcom.tablemessages.{PlayerInfoMessage, StandardTableAllInfoMessage, StandardTableInfoMessage}
import tables.StandardTable.PlayerInfo

import scala.collection.mutable
import scala.util.Random

final class StandardTable private (
                            val host: String,
                            val name: String,
                            val password: Option[String],
                            val server: Server
                            ) extends Table {

  /**
   * A dummy Int to quickly filter actual players to the server.
   */
  private val enterPassword: Int = Random.nextInt()

  val gameMode: GameMode = StandardMode

  type T = StandardTable.PlayerInfo

  protected val playersInfo: mutable.Map[String, StandardTable.PlayerInfo] = mutable.Map()

  def receivedPlayerInfo(playerName: String, ready: Boolean, abilities: List[Int], team: Int): Unit = {
    if (isPlayerSeated(playerName)) {
      playersInfo += playerName -> StandardTable.PlayerInfo(
        playerName, abilities, team, ready
      )

      broadcastTableInfo()
    }
  }

  def sendTableInfo(playerName: String, server: Server): Unit = {
    sendToPlayer(
      playerName,
      StandardTableAllInfoMessage(
        name,
        enterPassword,
        gameMode.toString,
        playersInfo.values.toList.sortBy(_.playerName)
          .zipWithIndex.zip(Table.definedColors).map({ case ((info, id), color) =>
            StandardTableInfoMessage(
              name,
              info.playerName,
              id.toLong,
              info.team,
              info.abilities,
              info.ready,
              Vector(color.r / 255.0, color.g / 255.0, color.b / 255.0)
            )
        })
      ),
      server
    )
  }

  private def findUnusedTeam: Int = {
    val usedTeams = playersInfo.values.map(_.team).toSet

    def acc(teamNbr: Int): Int = {
      if (usedTeams.contains(teamNbr)) acc(teamNbr + 1) else teamNbr
    }

    acc(1)
  }

  def newInfo(playerName: String): StandardTable.PlayerInfo = PlayerInfo(
    playerName, List(), findUnusedTeam, ready = false
  )

  def receivedPlayerInfo(message: PlayerInfoMessage): Unit = message match {
    case StandardTableInfoMessage(_, playerName, _, team, abilities, ready, _) =>
      playersInfo += playerName -> StandardTable.PlayerInfo(playerName, abilities, team, ready)
    case _ =>
      throw new ShouldNotReceiveThisMessageException
  }

}

object StandardTable {

  def apply(host: String, name: String, password: Option[String], server: Server): StandardTable = {
    new StandardTable(host, name, password, server)
  }


  final case class PlayerInfo(
                               playerName: String,
                               abilities: List[Int],
                               team: Int,
                               ready: Boolean
                             ) extends Table.PlayerInfo



}
