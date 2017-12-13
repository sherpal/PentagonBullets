package communication

import abilities.Ability
import gamemenusui.{GameSettingsPage, UIPages}
import gamemode.{CaptureTheFlagMode, GameMode}
import networkcom._
import networkcom.messages._
import org.scalajs.dom
import org.scalajs.dom.html
import sharednodejsapis.IPCRenderer
import ui.UI


class Host(val name: String, val gameName: String, val address: String, val port: Int, registrationId: Int,
           val gameMode: GameMode)
  extends PlayerSocket {

//  val panel: InGamePanel = UI.hostPanel
  val page: GameSettingsPage = UIPages.host

  protected val abilitySelect: html.Select = page.ability
  resetAbilityOptions()
  protected val abilityOptions: List[html.Option] = Ability.playerChoices.map(addAbilityOption)
  initializePanel()

  connect()

  private val maxNbrPlayers: Int = 10

  private def mayLaunch: Boolean =
    nbrOfPlayers > 1 &&
    nbrOfPlayers <= maxNbrPlayers &&
    notReadyPlayers.isEmpty &&
    teams.size > 1 &&
      (gameMode != CaptureTheFlagMode || teams.size == 2)

  private def launchGame(): Unit =
    sendReliable(LaunchGame(gameName))

  /**
   * The submit event is triggered when the launch button is pressed, which is why we don't access the
   * launch button, we don't need to.
   */
  page.form.onsubmit = (event: dom.Event) => {
    event.preventDefault()

    val notReady = notReadyPlayers

    if (mayLaunch) {
      if (teams.values.map(_.nbrOfPlayers).toList.distinct.length == 1) {
        launchGame()
      } else {
        UI.showConfirmBox(
          "Teams", "All teams does not have the same number of players. Launch anyway?", if (_) launchGame()
        )
      }
    } else if (nbrOfPlayers == 1) {
      UI.showAlertBox("Missing players", "You can't play alone.")
    } else if (nbrOfPlayers > maxNbrPlayers) {
      UI.showAlertBox("Too many players", s"Maximum number of players is $maxNbrPlayers.")
    } else if (teams.size <= 1) {
      UI.showAlertBox("Missing teams", "There has to be at least two teams.")
    } else if (notReady.nonEmpty) {
      if (notReady.tail.isEmpty) {
        UI.showAlertBox("Player not ready", notReady.head + " is not ready.")
      } else {
        UI.showAlertBox("Players not ready", "The following players are not ready:\n" + notReady.mkString("\n"))
      }
    } else if (gameMode == CaptureTheFlagMode && teams.size == 2) {
      UI.showAlertBox("Team number", s"Only two teams are allowed in $gameMode mode.")
    }

    false
  }


  private var _gameId: Long = 0

  def messageCallback(client: Client, msg: Message): Unit = {
    msg match {
      case PlayerReady(_, playerName, status) =>
        playerReady(playerName, status)
        if (!scala.scalajs.LinkingInfo.developmentMode) {
          if (status && notReadyPlayers.forall(_ == name)) {
            IPCRenderer.send("flash-window")
          }
        }

      case ChoseTeam(_, playerName, team) =>
        changeTeamNumber(playerName, team)

      case ChoseAbility(_, playerName, abilityId) =>
        changeAbility(playerName, abilityId, added = true)

      case DoNotChoseAbility(_, playerName, abilityId) =>
        changeAbility(playerName, abilityId, added = false)

      case GameLaunched(_, password, playerInfo) =>
        launchGameCallback(password, playerInfo)

      case CancelGame(_) =>
        cancelGameCallback()

      case CurrentPlayers(_, info) =>
        updatePlayerList(info)
        //sendReadyStatus()

      case GameCreated(_, id) =>
        _gameId = id
        println("Game has been created successfully")
        sendReliable(NewPlayerArrives(gameName, name, 0))

      case _ =>
        dom.console.warn(s"Received $msg but I don't know what it is. I'll juste ignore it.")
    }
  }

  def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    if (connected) {
      sendReliable(NewGameCreation(gameName, name, registrationId, gameMode.toString))
    } else {
      if (!gameWasLaunched) println("I have been disconnected from the GameServer :(")
    }
  }

}


