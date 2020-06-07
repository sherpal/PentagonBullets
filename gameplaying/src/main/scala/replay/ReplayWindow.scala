package replay

import globalvariables.{BaseDirectory, DataStorage}
import renderermainprocesscom.{Message, ReadyToShow}
import sharednodejsapis._

import scala.scalajs.js.UndefOr

class ReplayWindow {

  val window: BrowserWindow = new BrowserWindow(
    new BrowserWindowOptions {
      override val width: UndefOr[Int]  = if (scala.scalajs.LinkingInfo.developmentMode) 1400 else 1000
      override val height: UndefOr[Int] = 800

      override val minWidth: UndefOr[Int]  = 1000
      override val minHeight: UndefOr[Int] = 700

      override val resizable: UndefOr[Boolean] = true

      override val show: UndefOr[Boolean] = false
    }
  )

  IPCRenderer.once(
    "main-renderer-message",
    (_: IPCMainEvent, msg: Any) => {
      Message.decode(msg.asInstanceOf[scala.scalajs.js.Array[Byte]]) match {
        case ReadyToShow(id) if window.id == id =>
          println("The replay window has been loaded")
        case _ =>
      }
    }
  )

  window.loadURL(
    "file://" + Path.join(
      DataStorage.retrieveGlobalValue("baseDirectory").asInstanceOf[BaseDirectory].directory,
      "/gameplaying/gameplaying/replay.html"
    )
  )

  if (scala.scalajs.LinkingInfo.developmentMode) {
    window.webContents.openDevTools()
  }

  window.setMenu(null)

}
