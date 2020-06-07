package parsinginputs

import exceptions.{IllegalPortChoice, MalformedExpressionInInput}
import org.scalajs.dom
import org.scalajs.dom.html
import ui.UI

/**
  * It has one method that returns all information contained in host game or join game fields.
  */
object RetrieveInfo {

  def retrievePlayerName(playerElement: html.Input): String = {
    val playerName = playerElement.value.trim

    try {
      if (playerName == "") {
        throw MalformedExpressionInInput("Player name is empty.")
      } else if (playerName.length > 15) {
        throw MalformedExpressionInInput("Player name must have 15 characters at most.")
      }
      playerName
    } catch {
      case e: MalformedExpressionInInput =>
        dom.console.error(e.msg)
        UI.showAlertBox("Player Name", e.msg)
        ""
      case _: Throwable =>
        UI.showAlertBox("Malformed name.", "")
        ""
    }
  }

  def retrieveGameName(gameNameElement: html.Input): String = {
    val gameName = gameNameElement.value.trim

    try {
      if (gameName == "") {
        throw MalformedExpressionInInput("Game Name is empty.")
      }

      gameName
    } catch {
      case e: MalformedExpressionInInput =>
        UI.showAlertBox("Game name", e.msg)
        ""
      case _: Throwable =>
        UI.showAlertBox("Malformed game name.", "")
        ""
    }
  }

  def retrieveAddress(addressElement: html.Input): String = {
    val addressContent = addressElement.value

    try {
      if (addressContent == "") {
        throw MalformedExpressionInInput("Address is empty.")
      }

      addressContent
    } catch {
      case e: MalformedExpressionInInput =>
        UI.showAlertBox("Address", e.msg)
        ""
      case _: Throwable =>
        UI.showAlertBox("Malformed address", "")
        ""
    }

  }

  def retrievePortNumber(portElement: html.Input): Int =
    try {
      val portContent = portElement.value.toInt

      if (portContent < 1024 || portContent > 65535) {
        throw IllegalPortChoice(s"Port number should be comprised between 1024 and 65535 (actual: $portContent)")
      }

      portContent
    } catch {
      case e: IllegalPortChoice =>
        UI.showAlertBox("Port", e.msg)
        0
      case _: Throwable =>
        UI.showAlertBox("Malformed port number", "")
        0
    }

  def apply(): (String, String, String, Int) =
    try {

      val playerName     = dom.document.getElementById("inputName").asInstanceOf[html.Input].value.trim
      val gameName       = dom.document.getElementById("inputGameName").asInstanceOf[html.Input].value.trim
      val addressContent = dom.document.getElementById("inputAddress").asInstanceOf[html.Input].value.trim

      if (playerName == "") {
        throw MalformedExpressionInInput("Player name is empty.")
      } else if (playerName.length > 15) {
        throw MalformedExpressionInInput("Player name must have 15 characters at most.")
      } else if (gameName == "") {
        throw MalformedExpressionInInput("Game Name is empty.")
      } else if (addressContent == "") {
        throw MalformedExpressionInInput("Address is empty.")
      }

      val portContent = dom.document.getElementById("inputPort").asInstanceOf[html.Input].valueAsNumber
      if (portContent < 1024 || portContent > 65535) {
        throw IllegalPortChoice(s"Port number should be comprised between 1024 and 65535 (actual: $portContent)")
      }

      (playerName, gameName, addressContent, portContent.toInt)

    } catch {
      case illegalPort: IllegalPortChoice =>
        val portError = dom.document.getElementById("portError").asInstanceOf[html.Div]
        portError.style.visibility = "visible"
        portError.innerHTML        = illegalPort.msg
        ("", "", "", 0)
      case e: MalformedExpressionInInput =>
        dom.console.error(e.msg)
        dom.window.alert(e.msg)
        ("", "", "", 0)
      case _: Throwable =>
        dom.console.error("An error occurred in the onclick event")
        ("", "", "", 0)
    }

}
