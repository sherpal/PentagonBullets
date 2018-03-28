package joined

import communication.TableClient
import globalvariables._
import networkcom.PlayerGameSettingsInfo
import networkcom.tablemessages._
import org.scalajs.dom
import org.scalajs.dom.html
import popups.Alert
import renderer.Tabs


/**
 * Manages everything when we joined a table, either by creating one, or when joining an existing one.
 */
object GameSettings {

  private val launchGameButton: html.Button = dom.document.getElementById("launchGameButton").asInstanceOf[html.Button]


  launchGameButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => {

    // TODO: check that game can actually be launched

    List[(Boolean, () => String)](
      _playersInfo.exists(!_.ready) -> (() => "All players need to be ready."),
      (_playersInfo.lengthCompare(2) < 0) -> (() => "You can't play alone."),
      (_playersInfo.map(_.team).toSet.size < 2) -> (() => "There must be at least 2 teams.")
    ).find(_._1) match {
      case Some((_, errorMessage)) =>
        Alert.showAlert(
          "Failed to launch game",
          errorMessage()
        )
      case None =>
        TableClient.sendOrderedReliable(
          LaunchGameFromTable(_joinedTable.get, success = true, "")
        )
    }


  })


  private var _joinedTable: Option[String] = None
  private var _playerName: Option[String] = None
  private var _host: Option[Boolean] = None


  private var gameData: Option[GameData] = None
  private var _playersInfo: List[PlayerGameSettingsInfo] = Nil

  /**
   * Manages what happens when joining (or creating) a table
   * @param tableName  name of the table, necessary to contact the server
   * @param playerName name of the player
   * @param host       whether this player opened the table. If not, we need to hide the Launch Game Button.
   */
  def joinTable(tableName: String, playerName: String, host: Boolean = false, nbrOfPlayer: Int = 1): Unit = {
    if (_joinedTable.isEmpty) {
      _joinedTable = Some(tableName)

      UserSendingInfoStandard.resetTeamChoices()

      Tabs.switchTab(Tabs.gameSettingsTab)

      gameNameHeading.textContent = tableName
      playerNameHeading.textContent = playerName

      TableClient.sendOrderedReliable(
        AskTableInfo(tableName, playerName)
      )

      _playerName = Some(playerName)
      _host = Some(host)

      launchGameButton.style.display = if (host) "inline" else "none"
    }
  }


  dom.document.getElementById("quitStandardTable").addEventListener("click", (_: dom.Event) => {
    leaveTable()
  })

  def leaveTable(): Unit = {

    _joinedTable match {
      case Some(table) =>
        TableClient.sendReliable(LeaveTable(table))
      case None =>
    }

    _joinedTable = None

    Tabs.switchTab(Tabs.tableListTab)

  }


  def tableWasDestroyed(message: TableDestroyed): Unit = {
    leaveTable()
  }


  private val gameNameHeading: html.Heading = dom.document.getElementById("gameName").asInstanceOf[html.Heading]
  private val gameModeHeading: html.Heading = dom.document.getElementById("gameMode").asInstanceOf[html.Heading]
  private val playerNameHeading: html.Heading =
    dom.document.getElementById("playerNameHeading").asInstanceOf[html.Heading]


  def receivedTableInfoMessage(message: TableServerMessages): Unit = message match {
    case StandardTableAllInfoMessage(tableName, tableEnterPassword, mode, playersInfo) if _joinedTable.isDefined &&
      _joinedTable.get == tableName =>
      gameModeHeading.textContent = mode

      println("received table info")

      /**
       * Recording the TableClient address and port so that, if a server needs to be created, it directly get access to
       * it.
       */
      gameData = Some(
        GameData(
          tableName,
          _playerName.get,
          tableEnterPassword,
          TableClient.address,
          TableClient.port,
          _host.get,
          mode
        )
      )

      _playersInfo = playersInfo.map(info => PlayerGameSettingsInfo.fromInfo(
        info.playerName,
        info.ready,
        info.team,
        info.playerId,
        info.abilities,
        (info.color(0), info.color(1), info.color(2))
      ))

      PlayerLine.removePlayerLinesNotIn(_playersInfo.map(_.playerName))

      _playersInfo.map(info => (info, PlayerLine.playerLine(info.playerName)))
        .map({
          case (info, None) =>
            (info, new PlayerLine(info.playerName))
          case (info, Some(line)) =>
            (info, line)
        })
        .foreach({
          case (info, line) =>
            line.setReady(info.ready)
            line.setTeam(info.team)
        })

      PlayerLine.showPlayerLines()

      _playerName match {
        case Some(name) =>
          _playersInfo.find(_.playerName == name) match {
            case Some(info) =>
              UserSendingInfoStandard.setReady(info.ready)
            case None =>
              dom.console.warn("Received player information of my table without me :(")
          }
        case None =>
          dom.console.warn("Player received information of a table where their are not seated...")
      }

      UserSendingInfoStandard.setTeamChoices(_playersInfo.length)

    case _ =>
      println("not yet implemented")
  }

  def receivedCreateClientMessage(message: CreateClient): Unit = {

    VariableStorage.storeValue("password", gameData.get.reservationId)

    DataStorage.storeValue("tableServerPeer", PeerData(TableClient.address, TableClient.port))

    DataStorage.storeValue("gameData", GameData(
      gameData.get.gameName,
      gameData.get.playerName,
      gameData.get.reservationId,
      message.peer.address,
      message.peer.port,
      _host.get,
      gameData.get.gameMode
    ))

    DataStorage.storeValue("playerGameSettings", PlayerDataList(_playersInfo.map(info => PlayerData(
      _joinedTable.get,
      info.playerName,
      info.id,
      info.team,
      info.ready,
      info.abilities,
      Vector(info.color._1, info.color._2, info.color._3)
    ))))

    dom.window.location.href = "../../gameplaying/gameplaying/gameplayinginterface.html"

  }

  def receivedCreateServerMessage(msg: CreateServer): Unit = {

    DataStorage.storeGlobalValue("gameData", OneTimeServerGameData(
      gameData.get.gameName,
      gameData.get.gameMode,
      gameData.get.reservationId,
      gameData.get.address,
      gameData.get.port,
      _playersInfo.map(info => {
        PlayerData(
          gameData.get.gameName,
          info.playerName,
          info.id,
          info.team,
          info.ready,
          info.abilities,
          Vector(info.color._1, info.color._2, info.color._3)
        )
      })
    ))

    renderermainprocesscom.Message.sendMessageToMainProcess(
      renderermainprocesscom.OpenOneTimeServer()
    )

  }




}
