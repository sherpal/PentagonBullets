package mainprocess

import sharednodejsapis._

import scala.scalajs.js.UndefOr
import electron.{App, BrowserWindowMainProcess, MainProcessGlobals}
import globalvariables.WindowId
import org.scalajs.dom.raw.Event
import renderermainprocesscom.{CloseTooltip, MoveTooltip, OpenTooltip}
import tooltip.Tooltip

import scala.collection.mutable



object MainProcess {
  private var mainWindow: BrowserWindowMainProcess = _

  val windows: mutable.Set[BrowserWindowMainProcess] = mutable.Set()

  def main(args: Array[String]): Unit = {

    // Need to create the Storage object in order to use it.
    Storage



    def createWindow(): Unit = {

      val win = new BrowserWindowMainProcess(new BrowserWindowOptions {
        override val width: UndefOr[Int] = 1400
        override val height: UndefOr[Int] = 800

        override val minWidth: UndefOr[Int] = 1000
        override val minHeight: UndefOr[Int] = 700

        override val show: UndefOr[Boolean] = false

//        override val icon: UndefOr[String] = Path.join(
//          Path.dirname(js.Dynamic.global.myGlobalDirname.asInstanceOf[String]), "/assets/icon/pentagonBulletsIcon.ico"
//        )

        override val resizable: UndefOr[Boolean] = true

      })

      win.loadURL(
        "file://" + Path.join(
          Path.dirname(MainProcessGlobals.__dirname), "/gamemenus/mainscreen/index.html"
        )
      )

      if (scala.scalajs.LinkingInfo.developmentMode)
        win.webContents.openDevTools()

      win.webContents.on("did-finish-load", () => {
        Storage.storeVariable(win.webContents, "windowId", WindowId(win.id))
      })

      win.once("ready-to-show", () => scala.scalajs.js.timers.setTimeout(1000) { win.show() })

      win.setMenu(null)

      mainWindow = win
    }

    App.on("ready", () => {
      createWindow()
      Tooltip
    })

    App.on("window-all-closed", () => App.quit())

    App.on("browser-window-created", (_: Event, window: BrowserWindowMainProcess) => {
      windows += window
      window.once("closed", () => {
        windows -= window
        if (window == mainWindow) {
          Tooltip.close()
        }
      })
    })

    IPCMain.on("testing", (_: IPCMainEvent, a: Any) => {println(a)})

    IPCMain.on("flash-window", (event: IPCMainEvent) => {
      val senderWindow = BrowserWindowMainProcess.fromWebContents(event.sender)
      if (senderWindow != BrowserWindowMainProcess.getFocusedWindow()) {
        BrowserWindowMainProcess.fromWebContents(event.sender).flashFrame(true)
      }
    })


    IPCMain.on("main-renderer-message", (_: IPCMainEvent, msg: Any) => {
      renderermainprocesscom.Message.decode(msg.asInstanceOf[scala.scalajs.js.Array[Byte]]) match {
        case OpenTooltip(text, x, y) =>
          Tooltip.showTooltip(text, x, y)
        case MoveTooltip(x, y) =>
          Tooltip.moveTooltip(x, y)
        case CloseTooltip() =>
          Tooltip.hideTooltip()
        case _ => println(msg)
      }
    })

  }
}
