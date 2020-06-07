package parsinginputs

import gamemenusui.UIMenuPanels
import io.{ConnectionToGameInfo, IO}
import ui.UI

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Can save and load connection info
  */
object SaveAndLoadConnectionInfo {

  def load(host: Boolean): Unit = {
    val fileName = "connectionInfo.sav"
    IO.open(s"/saved/$fileName")
      .onComplete({
        case Success(fd) =>
          IO.close(fd)
          val recordedInfo = IO.readFileContent(s"/saved/$fileName")
          recordedInfo.onComplete({
            case Success(content) =>
              content match {
                case ConnectionToGameInfo(pseudo, gameName, address, port) =>
                  UI.playerName.value = pseudo
                  val form = if (host) {
                    UIMenuPanels.host
                  } else {
                    UIMenuPanels.join
                  }
                  form.gameName.value = gameName
                  form.port.value     = port.toString
                  form.address.value  = address
                case _ =>
              }
            case _ =>
          })
        case _ =>
      })
  }

  def save(host: Boolean, pseudo: String, gameName: String, address: String, port: Int): Unit = {
    val fileName = "connectionInfo.sav"
    IO.mkdir("saved")
      .andThen({
        case _ =>
          IO.writeFileContent(
            s"/saved/$fileName",
            ConnectionToGameInfo(pseudo, gameName, address, port),
            (_) => if (scala.scalajs.LinkingInfo.developmentMode) println("could not save connection data :/")
          )
      })

  }

}
