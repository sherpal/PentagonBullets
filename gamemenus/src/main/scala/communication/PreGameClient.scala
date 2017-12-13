package communication

import exceptions.DoesNotManageThisMessage
import gamemenusui.UIPages
import globalvariables.{DataStorage, GameData}
import networkcom._
import networkcom.messages._
import org.scalajs.dom
import org.scalajs.dom.html
import ui.UI

import scala.scalajs.js.timers.{clearTimeout, setTimeout}

/**
 * The PreGameClient will communicate to the server before creating or joining a game.
 *
 * It can be used to check whether
 * - a game name exists (good for joining, bad for creating)
 * - a game name has been booked (na for joining, bad for creating)
 * - a player name exists in some game (bad for joining, na for creating)
 *
 * @param address address of the server (can be "localhost")
 * @param port    port the server is listening to
 */
class PreGameClient(val address: String, val port: Int, playerName: String, gameName: String, host: Boolean)
  extends Client {


  private var _waitingAnswer: Boolean = true
  def waitingForAnswer: Boolean = _waitingAnswer

  connect()

  private val connectionHandle = setTimeout(2000) {
    println("did not manage to connect")
    endConnection()
    UI.showAlertBox("Connection error", "Did not manage to connect to server.")
  }

  private def endConnection(): Unit = {
    _waitingAnswer = false
    disconnect()
    clearTimeout(connectionHandle)
  }

  private def storeInfo(gName: String, gameMode: String, reservationId: Int): Unit = {
    DataStorage.storeValue("gameData", GameData(gameName, playerName, reservationId, address, port, host, gameMode))
  }


  def connectedCallback(client: Client, peer: Peer, status: Boolean): Unit = {
    if (status) {
      println("connected")
      if (host) {
        sendOrdered(ReserveGameName(
          gameName, dom.document.getElementsByName("mode")(0).asInstanceOf[html.Select].value
        ))
      } else {
        sendOrdered(ReservePlayerName(gameName, playerName))
      }
    } else if (waitingForAnswer) {
      endConnection()
    }
  }


  def messageCallback(client: Client, msg: Message): Unit = {
    try {
      if (scala.scalajs.LinkingInfo.developmentMode) println(s"received $msg")

      msg match {
        case GameNameReserved(name, gameMode, reservationId, errorMessage) =>
          errorMessage match {
            case Some(message) =>
              UI.showAlertBox("Game Creation Failed", message)
            case None =>
              if (scala.scalajs.LinkingInfo.developmentMode) println("should proceed to next web page")
              storeInfo(name, gameMode, reservationId)
//              UI.closeMenuPanel(UI.hostMenuPanel)
              PlayerSocket.setCurrentSocket(
                new Host(playerName, gameName, address, port, reservationId, gameMode)
              )

              UI.freeze(UI.playerName)
              UIPages.host.open()




//              scala.scalajs.js.timers.setTimeout(500) {
//
//                UI.switchPanel(UI.hostPanel)
//                UI.freeze(UI.playerName)
//
//              }
          }
          endConnection()

        case PlayerNameReserved(gName, gameMode, reservationId, errorMessage) =>
          errorMessage match {
            case Some(message) =>
              UI.showAlertBox("Join Game Failed", message)
            case None =>
              if (scala.scalajs.LinkingInfo.developmentMode) println("should proceed to next web page")
              storeInfo(gName, gameMode, reservationId)
//              UI.closeMenuPanel(UI.joinMenuPanel)
              PlayerSocket.setCurrentSocket(
                new PlayerClient(playerName, gameName, address, port, reservationId, gameMode)
              )

              UI.freeze(UI.playerName)
              UIPages.join.open()
//              scala.scalajs.js.timers.setTimeout(500) {
//
//                UI.switchPanel(UI.joinPanel)
//                UI.freeze(UI.playerName)
//
//              }
          }
          endConnection()

        case GameDoesNotExist(gName) =>
          if (scala.scalajs.LinkingInfo.developmentMode) println(s"game $gName does not exist.")
          UI.showAlertBox("Game name", s"Game `$gName` does not exist.")
          endConnection()

        case TestMessage(message) =>
          if (scala.scalajs.LinkingInfo.developmentMode) println(message)

        case _ =>
          throw DoesNotManageThisMessage(s"Message type was ${msg.getClass}")
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        UI.showAlertBox("Fatal error", "Try again to connect.")
    }
  }
}
