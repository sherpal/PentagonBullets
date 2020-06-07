package communication

import abilities.Ability
import entitiescollections.PlayerTeam
import gamemenusui.{GameSettingsPage, UIPages}
import gamemode.GameMode
import globalvariables.{DataStorage, PlayerData, PlayerDataList}
import globalvariables.VariableStorage._
import networkcom._
import networkcom.messages._
import org.scalajs.dom
import org.scalajs.dom.html
import sharednodejsapis.IPCRenderer
import ui.UI

import scala.scalajs.js
import scala.scalajs.js.timers.{clearTimeout, setTimeout, SetTimeoutHandle}

/**
  * A PlayerSocket is either the computer that hosts the game (the one who manages game settings) or a player joining a
  * game.
  */
trait PlayerSocket extends Client {
  val name: String

  val gameName: String

  //val panel: InGamePanel

  val page: GameSettingsPage

  val gameMode: GameMode

  protected def playerReady(playerName: String, status: Boolean): Unit =
    playersConnected.find(_.playerName == playerName) match {
      case Some(player) =>
        player.ready = status
        playerRow(playerName) match {
          case Some(row) =>
            row.className = if (status) "w3-green" else ""
          case None =>
            dom.console.warn(s"Didn't find the row attached to $playerName")
        }
      case None =>
        dom.console.warn(s"Player `$playerName` changed their ready status, but I don't know him.")
    }

  protected def sendReadyStatus(event: dom.Event): Unit =
    if (abilityOptions.exists(_.selected)) { //(abilityButtons.exists(_.checked)) {
      sendReliable(PlayerReady(gameName, name, page.ready.checked)) //panel.ready.checked))
      //sendReliable(PlayerReady(gameName, name, readyCheckBox.checked))
    } else {
      event.preventDefault()
      //dom.window.alert("You have to chose an ability before being ready!")
      UI.showAlertBox("Chose ability", "You have to chose an ability before being ready!")
    }

  protected val abilitySelect: html.Select

  protected def resetAbilityOptions(): Unit = {
    (for (j <- page.ability.children.length - 1 to 0 by -1) yield { page.ability.children(j) }).toList
      .filter(_.isInstanceOf[html.Option])
      .map(_.asInstanceOf[html.Option])
      .filter(_.value != "")
      .foreach(page.ability.removeChild(_))

    page.ability.value = ""
  }

  protected def addAbilityOption(abilityId: Int): html.Option = {
    val option: html.Option = dom.document.createElement("option").asInstanceOf[html.Option]
    //option.id = s"abilityOption$abilityId"
    option.value = abilityId.toString

    page.ability.add(option)

    val abilityName: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
    abilityName.textContent = Ability.abilityNames(abilityId)
    option.appendChild(abilityName)

    option
  }

  protected val abilityOptions: List[html.Option]

  protected def changeAbility(playerName: String, abilityId: Int, added: Boolean): Unit =
    playersConnected.find(_.playerName == playerName) match {
      case Some(player) =>
        if (added) {
          player.abilities :+= abilityId
        } else {
          player.abilities = player.abilities.filter(_ != abilityId)
        }
      case None =>
        dom.console.warn(s"$playerName wants to change an ability ($abilityId), but I don't know this guy...")
    }

  private def sendTeamNumber(): Unit = {
    val team = try {
      page.team.value.toInt
    } catch {
      case _: Throwable => -1
    }
    if (team > 0 && team <= playersConnected.length && team != thisPlayer.team) {
      sendOrderedReliable(ChoseTeam(gameName, name, team))
    }
  }

  private def updateTeamSelect(): Unit = {
    val value = thisPlayer.team

    page.team.innerHTML = ""

    for (j <- 1 to playersConnected.size) {
      val option = dom.document.createElement("option").asInstanceOf[html.Option]
      option.value = j.toString

      val text: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
      text.textContent = s"Team $j"

      option.appendChild(text)

      page.team.appendChild(option)
    }

    page.team.value = value.toString
  }

  private def thisPlayer: PlayerGameSettingsInfo = playersConnected.find(_.playerName == name).get

  protected def changeTeamNumber(playerName: String, team: Int): Unit =
    playersConnected.find(_.playerName == playerName) match {
      case Some(player) =>
        player.team = team
        updatePlayerList()
      case None =>
        dom.console.warn(s"$playerName wants to chose a team ($team), but I don't know this guy...")
    }

  private var leaveGameHandle: Option[SetTimeoutHandle] = None

  protected def cancelGameCallback(): Unit = {
    leaveGame(s"Game `$gameName` has been canceled.")
    UI.unfreeze(UI.playerName)
  }

  protected def leaveGameCallback(): Unit = {
    leaveGame(s"You have been disconnected from Game `$gameName`.")
    UI.unfreeze(UI.playerName)
  }

  private def leaveGame(msg: String): Unit = {
    if (leaveGameHandle.isDefined)
      clearTimeout(leaveGameHandle.get)
    leaveGameHandle = None

    disconnect()

    unStoreInfo()
    storeValue("gameCanceled", msg)

    UI.unfreeze(UI.playerName)

    //dom.window.location.href = "../mainscreen/mainscreen.html"
    UI.showAlertBox("Disconnected", s"You have been disconnected from Game `$gameName`.")
    //UI.switchPanel(UI.menuPanel)
    UIPages.mainMenu.open()
  }

  protected var gameWasLaunched: Boolean = false

  protected def launchGameCallback(password: Int, info: Array[SendPlayerInfo]): Unit = {
    if (scala.scalajs.LinkingInfo.developmentMode)
      println("we should launch the game.")

    gameWasLaunched = true
    disconnect()

    storeValue("password", password)

    DataStorage.storeValue(
      "playerGameSettings",
      PlayerDataList(
        info
          .map(
            infoPiece =>
              PlayerData(
                infoPiece.gameName,
                infoPiece.playerName,
                infoPiece.id,
                infoPiece.team,
                infoPiece.ready,
                infoPiece.abilities,
                infoPiece.color
              )
          )
          .toList
      )
    )

    if (!scala.scalajs.LinkingInfo.developmentMode) {
      IPCRenderer.send("flash-window")
    }

    dom.window.location.href = "../../gameplaying/gameplaying/gameplayinginterface.html"
  }

  private def unStoreInfo(): Unit =
    DataStorage.unStoreValue("gameData")

  private var playersConnected: List[PlayerGameSettingsInfo] = List()

  def teams: Map[Int, PlayerTeam] =
    playersConnected.groupBy(_.team).map(elem => elem._1 -> new PlayerTeam(elem._2.head.team, elem._2.map(_.id)))

  def updatePlayersConnected(playersInfo: Iterable[SendPlayerInfo]): Unit =
    playersConnected = playersInfo.toList.map(info => {
      val player = new PlayerGameSettingsInfo(info.playerName)
      player.id    = info.id
      player.team  = info.team
      player.ready = info.ready
      info.abilities.foreach(player.abilities :+= _)
      player
    })

  private def playersConnectedTableBody: html.Table =
    page.tablePlayers.getElementsByTagName("tbody").apply(0).asInstanceOf[html.Table]

  private def addPlayerInfo(playerInfo: PlayerGameSettingsInfo): Unit = {
    val tr = dom.document.createElement("tr").asInstanceOf[html.TableRow]

    val nameCell = dom.document.createElement("td").asInstanceOf[html.TableCol]
    nameCell.textContent = playerInfo.playerName
    tr.appendChild(nameCell)

    val teamCell = dom.document.createElement("td").asInstanceOf[html.TableCol]
    teamCell.textContent = playerInfo.team.toString
    tr.appendChild(teamCell)

    tr.className = if (playerInfo.ready) "w3-green" else ""

    playersConnectedTableBody.appendChild(tr)
  }

  private def tableBodyRows: IndexedSeq[html.TableRow] = {
    val children = playersConnectedTableBody.children
    (0 until children.length).map(children(_).asInstanceOf[html.TableRow])
  }
  private def playerRow(playerName: String): Option[html.TableRow] =
    tableBodyRows.find((tr: html.TableRow) => {
      tr.children(0).textContent == playerName
    })

  protected def nbrOfPlayers: Int             = playersConnected.length
  protected def notReadyPlayers: List[String] = playersConnected.filterNot(_.ready).map(_.playerName)

  protected def updatePlayerList(): Unit = {
    playersConnectedTableBody.innerHTML = ""

    playersConnected = playersConnected.sortBy(_.playerName)
    playersConnected.foreach(addPlayerInfo)
    updateTeamSelect()
  }

  protected def updatePlayerList(playersInfo: Iterable[SendPlayerInfo]): Unit = {
    updatePlayersConnected(playersInfo)
    updatePlayerList()
  }

  protected def initializePanel(): Unit = {
    page.quitButton.removeEventListener("click", PlayerSocket.quitButtonClick)
    page.quitButton.addEventListener("click", PlayerSocket.quitButtonClick)

    page.ability.removeEventListener("change", PlayerSocket.abilitySelectChange)
    page.ability.addEventListener("change", PlayerSocket.abilitySelectChange)

    page.gameName.innerHTML = gameName + s" (mode: $gameMode)"

    page.ready.checked = false
    page.ready.removeEventListener("click", PlayerSocket.readyOnClick)
    page.ready.addEventListener[dom.MouseEvent]("click", PlayerSocket.readyOnClick)

    page.team.removeEventListener("change", PlayerSocket.teamOnChange)
    page.team.addEventListener[dom.Event]("change", PlayerSocket.teamOnChange)
  }
}

object PlayerSocket {

  private var _currentSocket: Option[PlayerSocket] = None

  def currentSocket: PlayerSocket = _currentSocket.get

  def setCurrentSocket(socket: PlayerSocket): Unit = {
    _currentSocket match {
      case Some(previousSocket) =>
        previousSocket.disconnect()
      case _ =>
    }
    // things to do?

    _currentSocket = Some(socket)
  }

  private val teamOnChange: js.Function1[dom.Event, Unit] = (_: dom.Event) => currentSocket.sendTeamNumber()

  private val readyOnClick: js.Function1[dom.MouseEvent, Unit] = (e) => currentSocket.sendReadyStatus(e)

  private val quitButtonClick: js.Function1[dom.MouseEvent, Unit] = (_: dom.MouseEvent) => {

    UI.showConfirmBox(
      "Quit Game",
      "Are you sure you want to leave the game?",
      if (_) {
        currentSocket.sendReliable(LeaveGame(currentSocket.gameName, currentSocket.name))

        currentSocket.leaveGameHandle = Some(setTimeout(2000) {
          currentSocket.leaveGameCallback()
        })
      }
    )

  }

  private val abilitySelectChange: js.Function1[dom.MouseEvent, Unit] = (_: dom.Event) => {
    currentSocket.sendReliable(
      ChoseAbility(currentSocket.gameName, currentSocket.name, currentSocket.page.ability.value.toInt)
    )
    println(s"Choose ability ${currentSocket.page.ability.value.toInt}")
  }

}
