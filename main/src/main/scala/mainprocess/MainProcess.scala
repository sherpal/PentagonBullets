package mainprocess

import sharednodejsapis._

import scala.scalajs.js.UndefOr
import electron.{App, BrowserWindowMainProcess, MainProcessGlobals}
import gameinfo.GameInfoStorage
import globalvariables.WindowId
import org.scalajs.dom.raw.Event
import renderermainprocesscom._
import tooltip.Tooltip

import scala.collection.mutable
import scala.scalajs.js



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

    val mainRendererMessageCallback: js.Function2[IPCMainEvent, Any, Unit] = (event: IPCMainEvent, msg: Any) => {
      renderermainprocesscom.Message.decode(msg.asInstanceOf[scala.scalajs.js.Array[Byte]]) match {
        case storeGameInfo: StoreGameInfo =>
          GameInfoStorage.messageHandler(storeGameInfo, event.sender)
        case ShowAndHide(flag) =>
          val window = BrowserWindowMainProcess.fromWebContents(event.sender)
          if (flag) window.show() else window.hide()
        case CloseMe() =>
          BrowserWindowMainProcess.fromWebContents(event.sender).close()
        case OpenTooltip(text, x, y) =>
          Tooltip.showTooltip(text, x, y)
        case MoveTooltip(x, y) =>
          Tooltip.moveTooltip(x, y)
        case CloseTooltip() =>
          Tooltip.hideTooltip()
        case OpenOneTimeServer() =>
          val window = new BrowserWindowMainProcess(new BrowserWindowOptions {
            override val width: UndefOr[Int] = 600
            override val height: UndefOr[Int] = 600

            override val show: UndefOr[Boolean] = scala.scalajs.LinkingInfo.developmentMode
          })
          window.loadURL("file://" +
            Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String],
              "../one-time-server/html/server.html")
          )
          window.webContents.openDevTools()
        case ReadyToShow(_) =>
          val window = BrowserWindowMainProcess.fromWebContents(event.sender)
          // messaging the main window that a new window is ready to be shown
          renderermainprocesscom.Message.sendMessageToWebContents(
            mainWindow.webContents, ReadyToShow(window.id)
          )
          window.show()
        case OpenDevTools() =>
          event.sender.openDevTools()
        case _ => println(msg)
      }
    }

    IPCMain.on("main-renderer-message", mainRendererMessageCallback)

  }
}
