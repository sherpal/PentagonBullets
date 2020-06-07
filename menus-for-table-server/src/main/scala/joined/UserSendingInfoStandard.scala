package joined

import abilities.Ability
import communication.TableClient
import networkcom.messages.SendPlayerInfo
import org.scalajs.dom
import org.scalajs.dom.html
import popups.Alert

object UserSendingInfoStandard {

  private val readyButton: html.Button = dom.document.getElementById("readyButton").asInstanceOf[html.Button]

  private val abilitySelect: html.Select = dom.document.getElementById("abilities").asInstanceOf[html.Select]

  Ability.playerChoices
    .map(id => (id, Ability.abilityNames(id)))
    .foreach({
      case (id, abilityName) =>
        val option = dom.document.createElement("option").asInstanceOf[html.Option]
        abilitySelect.appendChild(option)
        option.textContent = abilityName
        option.value       = id.toString
    })

  resetAbilityChoice()

  private def chosenAbilities: List[Int] =
    List(
      Ability.activateShieldId
    ) ++ (if (abilitySelect.value.toInt != 0) List(abilitySelect.value.toInt) else Nil)

  abilitySelect.addEventListener("change", (_: dom.Event) => {
    sendInfo()
  })

  private val teamSelect: html.Select = dom.document.getElementById("teamSelect").asInstanceOf[html.Select]

  teamSelect.addEventListener("change", (_: dom.Event) => {
    sendInfo()
  })

  def resetTeamChoices(): Unit =
    for (_ <- 0 until teamSelect.children.length) teamSelect.removeChild(teamSelect.lastElementChild)

  def setTeamChoices(playerNumber: Int): Unit = {
    val options = teamSelect.children

    if (options.length == 0) {
      for (j <- 1 to playerNumber) {
        val option = dom.document.createElement("option").asInstanceOf[html.Option]
        teamSelect.appendChild(option)
        option.value       = j.toString
        option.textContent = j.toString
      }

      teamSelect.lastElementChild.asInstanceOf[html.Option].selected = true
    } else if (playerNumber < options.length) {
      val selectedTeam = teamSelect.value.toInt

      for (_ <- options.length - 1 to playerNumber by -1) teamSelect.removeChild(teamSelect.lastChild)

      if (selectedTeam > teamSelect.children.length) {
        teamSelect.lastElementChild.asInstanceOf[html.Option].selected = true
        sendInfo()
      }
    } else if (playerNumber > options.length) {
      for (j <- options.length + 1 to playerNumber) {
        val option = dom.document.createElement("option").asInstanceOf[html.Option]
        teamSelect.appendChild(option)
        option.value       = j.toString
        option.textContent = j.toString
      }
    }
  }

  def setReady(ready: Boolean): Unit =
    readyButton.className = if (ready) "ready" else "notReady"

  def resetAbilityChoice(): Unit = {
    val options = abilitySelect.children
    (for (j <- 1 until options.length) yield options(j).asInstanceOf[html.Option])
      .foreach(_.selected = false)
    options(0).asInstanceOf[html.Option].selected = true
  }

  readyButton.addEventListener(
    "click",
    (_: dom.MouseEvent) => {
      if (chosenAbilities.lengthCompare(2) < 0) {
        Alert.showAlert(
          "Choose ability",
          "You need to choose an ability before being ready."
        )
        readyButton.className = "notReady"
      } else if (readyButton.className.contains("notReady")) {
        readyButton.className = "ready"
        sendInfo()
      } else {
        readyButton.className = "notReady"
        sendInfo()
      }
    }
  )

  private def sendInfo(): Unit =
    TableClient.sendOrderedReliable(
      SendPlayerInfo(
        dom.document.getElementById("gameName").asInstanceOf[html.Heading].textContent,
        dom.document.getElementById("playerNameHeading").asInstanceOf[html.Heading].textContent,
        0,
        teamSelect.value.toInt,
        readyButton.className.contains("ready"),
        chosenAbilities,
        Vector(0, 0, 0)
      )
    )

}
