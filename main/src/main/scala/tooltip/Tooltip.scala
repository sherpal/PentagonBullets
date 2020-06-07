package tooltip

import electron.{BrowserWindowMainProcess, MainProcessGlobals}
import sharednodejsapis._

import scala.scalajs.js.UndefOr
import scala.scalajs.js.timers._

object Tooltip {

  private val tooltipWindow = new BrowserWindowMainProcess(new BrowserWindowOptions {

    override val show: UndefOr[Boolean] = false

    override val frame: UndefOr[Boolean] = false

    override val width: UndefOr[Int] = 220

    override val height: UndefOr[Int] = 100

    override val alwaysOnTop: UndefOr[Boolean] = true

    override val resizable: UndefOr[Boolean] = false

    override val focusable: UndefOr[Boolean] = false

  })

  tooltipWindow.loadURL(
    "file://" + Path.join(
      Path.dirname(MainProcessGlobals.__dirname),
      "/tooltip/html/tooltip.html"
    )
  )

  // TODO: private var fadingIntervalId: Option[Int] = None
  private var willAppearTimeoutId: Option[SetTimeoutHandle] = None

  def hideTooltip(): Unit = {
    if (willAppearTimeoutId.isDefined) {
      clearTimeout(willAppearTimeoutId.get)
    }
    setTimeout(300) {
      if (tooltipWindow.isVisible()) {
        tooltipWindow.hide()
      }
    }
  }

  def showTooltip(text: String, xMousePos: Int, yMousePos: Int): Unit = {

    tooltipWindow.setPosition(xMousePos + 5, yMousePos + 5)

    tooltipWindow.webContents.send("tooltip-text", text)

    willAppearTimeoutId = Some(setTimeout(500) {
      tooltipWindow.show()
      willAppearTimeoutId = None
    })
  }

  def moveTooltip(xMousePos: Int, yMousePos: Int): Unit = {
//    scala.scalajs.js.timers.setTimeout(500) {
//      tooltipWindow.setPosition(xMousePos + 100, yMousePos + 100)
//      tooltipWindow.setSize(220, h)
//      MainProcess.mainWindow.focus()
//    }
//    tooltipWindow.webContents.send("test", List(xMousePos, yMousePos).mkString(","))
  }

  IPCMain.on("tooltip-height", (_: IPCMainEvent, height: Int) => {
    tooltipWindow.setSize(150, height)
  })

  IPCMain.on("tooltip-error", (_: IPCMainEvent, error: Any) => {
    println(error)
  })

  def close(): Unit =
    tooltipWindow.close()

}
