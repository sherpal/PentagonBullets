package renderer

import communication.PreGameClient
import gamemenusui.UIMenuPanels
import org.scalajs.dom
import parsinginputs.{RetrieveInfo, SaveAndLoadConnectionInfo}
import ui.UI

/**
  * Manages what happens in join game html file.
  */
object JoinGame {

  SaveAndLoadConnectionInfo.load(host = false)

  private var preGameClient: Option[PreGameClient] = None

  UIMenuPanels.join.formElement.addEventListener[dom.Event](
    "submit",
    (event: dom.Event) => {
      event.preventDefault()

      val address    = RetrieveInfo.retrieveAddress(UIMenuPanels.join.address)
      val port       = RetrieveInfo.retrievePortNumber(UIMenuPanels.join.port)
      val gameName   = RetrieveInfo.retrievePlayerName(UIMenuPanels.join.gameName)
      val playerName = RetrieveInfo.retrievePlayerName(UI.playerName)

      SaveAndLoadConnectionInfo.save(host = false, playerName, gameName, address, port)
      if (playerName != "" && address != "" && port != 0 && gameName != "") {
        preGameClient match {
          case Some(client) =>
            if (client.waitingForAnswer) {
              println("client is still waiting for answer...")
            } else {
              preGameClient = Some(new PreGameClient(address, port, playerName, gameName, host = false))
            }
          case None =>
            preGameClient = Some(new PreGameClient(address, port, playerName, gameName, host = false))
        }

      }

      false
    }
  )

}
