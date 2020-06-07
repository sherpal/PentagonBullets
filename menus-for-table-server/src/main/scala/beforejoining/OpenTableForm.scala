package beforejoining

import communication.TableClient
import gamemode.GameMode
import joined.GameSettings
import networkcom.tablemessages.{OpenTable, TableOpened}
import org.scalajs.dom
import org.scalajs.dom.html
import popups.Alert

object OpenTableForm {

  private val div: html.Div = dom.document.getElementById("openTableDiv").asInstanceOf[html.Div]

  private val form: html.Form = dom.document.getElementById("openTableForm").asInstanceOf[html.Form]

  private val openTableButton: html.Button = dom.document.getElementById("openTableButton").asInstanceOf[html.Button]

  openTableButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => div.style.display = "block")

  form.addEventListener(
    "submit",
    (event: dom.Event) => {
      event.preventDefault()

      println(s"Table name: $chosenTableName")
      println(s"Game mode: $selectedGameMode")

      println("open table form submitted")

      List(
        (chosenPlayerName == "") -> (() => s"Chose a player name."),
        TableInfo.doesTableExist(chosenTableName) -> (() => s"Table $chosenTableName already exists.")
      ).find(_._1) match {
        case Some((_, errorMessage)) =>
          Alert.showAlert("Failed to open table", errorMessage())
        case None =>
          TableClient.sendOrderedReliable(
            OpenTable(chosenPlayerName, chosenTableName, selectedGameMode, "")
          )
      }

      false
    }
  )

  private val playerNameInput: html.Input = dom.document.getElementById("playerNameInput").asInstanceOf[html.Input]

  private val tableNameInput: html.Input = dom.document.getElementById("tableNameInput").asInstanceOf[html.Input]

  private val gameModeSelect: html.Select = dom.document.getElementById("gameModeSelect").asInstanceOf[html.Select]

  GameMode.gameModes.foreach(mode => {
    val option = dom.document.createElement("option").asInstanceOf[html.Option]
    option.value       = mode.toString
    option.textContent = mode.toString
    gameModeSelect.appendChild(option)
  })

  private val cancelButton: html.Button = dom.document.getElementById("cancelOpen").asInstanceOf[html.Button]
  cancelButton.addEventListener("click", (e: dom.Event) => {
    e.preventDefault()

    div.style.display = "none"
  })

  /**
    * Retrieving open table info.
    */
  private def selectedGameMode: String = gameModeSelect.value

  private def chosenTableName: String = tableNameInput.value

  private def chosenPlayerName: String = playerNameInput.value.trim

  def openTableMessage(message: TableOpened): Unit =
    if (message.success) {
      div.style.display = "none"

      println("Table was open.")

      GameSettings.joinTable(message.name, chosenPlayerName, host = true)

    } else {
      Alert.showAlert(
        "Failed to open table",
        message.errorMessage
      )
    }

}
