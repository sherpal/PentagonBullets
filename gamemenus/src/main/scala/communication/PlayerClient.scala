package communication

import abilities.Ability
import gamemenusui.{GameSettingsPage, UIPages}
import gamemode.GameMode
import networkcom._
import networkcom.messages._
import org.scalajs.dom
import org.scalajs.dom.html

/**
 * A PlayerClient joined a game
 */
class PlayerClient(val name: String, val gameName: String, val address: String, val port: Int, registrationId: Int,
                   val gameMode: GameMode)
  extends PlayerSocket {

//  val panel: InGamePanel = UI.joinPanel
  val page: GameSettingsPage = UIPages.join

  protected val abilitySelect: html.Select = page.ability
  resetAbilityOptions()
  protected val abilityOptions: List[html.Option] = Ability.playerChoices.map(addAbilityOption)
  initializePanel()

  connect()

  def messageCallback(client: Client, msg: Message): Unit = {
    msg match {
      case PlayerReady(_, playerName, status) =>
        playerReady(playerName, status)

      case ChoseTeam(_, playerName, team) =>
        changeTeamNumber(playerName, team)

      case ChoseAbility(_, playerName, abilityId) =>
        changeAbility(playerName, abilityId, added = true)

      case DoNotChoseAbility(_, playerName, abilityId) =>
        changeAbility(playerName, abilityId, added = false)

      case GameLaunched(_, password, playerInfo) =>
        launchGameCallback(password, playerInfo)

      case LeaveGame(_, playerName) if playerName == name =>
        leaveGameCallback()

      case CancelGame(_) =>
        cancelGameCallback()

      case CurrentPlayers(_, info) =>
        updatePlayerList(info)
        //sendReadyStatus()

      case GameCreated(_, _) =>
        dom.console.warn(s"Received $msg but shouldn't have. I'll just ignore it.")

      case _ =>
        dom.console.warn(s"Received $msg but I don't know what it is. I'll juste ignore it.")
    }
  }

  def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    if (connected) {
      sendReliable(NewPlayerArrives(gameName, name, registrationId))
    }
  }


}

