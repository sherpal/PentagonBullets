package renderer

import communication.PreGameClient
import gamemenusui.UIMenuPanels
import org.scalajs.dom
import parsinginputs.{RetrieveInfo, SaveAndLoadConnectionInfo}
import ui.UI

/**
  * Manage what happens in the host game html file.
  */
object HostGame {

  private var preGameClient: Option[PreGameClient] = None

  SaveAndLoadConnectionInfo.load(host = true)

  UIMenuPanels.host.formElement.addEventListener[dom.Event](
    "submit",
    (event: dom.Event) => {

//  UI.hostForm.form.addEventListener[dom.Event]("submit", (event: dom.Event) => {
      event.preventDefault()

//    val address = RetrieveInfo.retrieveAddress(UI.hostForm.address)
//    val port = RetrieveInfo.retrievePortNumber(UI.hostForm.port)
//    val gameName = RetrieveInfo.retrievePlayerName(UI.hostForm.gameName)
//    val playerName = RetrieveInfo.retrievePlayerName(UI.playerName)
      val address    = RetrieveInfo.retrieveAddress(UIMenuPanels.host.address)
      val port       = RetrieveInfo.retrievePortNumber(UIMenuPanels.host.port)
      val gameName   = RetrieveInfo.retrieveGameName(UIMenuPanels.host.gameName)
      val playerName = RetrieveInfo.retrievePlayerName(UI.playerName)

      SaveAndLoadConnectionInfo.save(host = true, playerName, gameName, address, port)
      if (playerName != "" && address != "" && port != 0 && gameName != "") {
        preGameClient match {
          case Some(client) =>
            if (client.waitingForAnswer) {
              println("client is still waiting for answer...")
            } else {
              preGameClient = Some(new PreGameClient(address, port, playerName, gameName, host = true))
            }
          case None =>
            preGameClient = Some(new PreGameClient(address, port, playerName, gameName, host = true))
        }

      }

      false

    }
  )

}
