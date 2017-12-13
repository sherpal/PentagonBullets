package renderer

import gamemenusui.UIMenuPanels
import globalvariables.VariableStorage
import org.scalajs.dom
import parsinginputs.RetrieveInfo
import sharednodejsapis.{BrowserWindow, BrowserWindowOptions, Path}
import ui.UI

import scala.scalajs.js

/**
 * Manage what happens in the create server html file.
 */
object CreateServer {


  UIMenuPanels.createServer.formElement.addEventListener[dom.Event]("submit", (event: dom.Event) => {
    event.preventDefault()

    val portContent = RetrieveInfo.retrievePortNumber(UIMenuPanels.createServer.port)

    if (portContent != 0) {
      createServer(portContent)
    }

    false
  })


  def createServer(port: Int): Unit = {
    VariableStorage.storeGlobalValue("serverPort", port.toString)

    val win = new BrowserWindow(new BrowserWindowOptions {
      override val width: js.UndefOr[Int] = 600
      override val height: js.UndefOr[Int] = 600
    })
    win.loadURL("file://" +
      Path.join(js.Dynamic.global.selectDynamic("__dirname").asInstanceOf[String],
        "../../server/server/server.html")
    )
    win.webContents.openDevTools()

    //dom.window.location.href = "../mainscreen/mainscreen.html"
    //UI.closeMenuPanel(UI.createServerMenuPanel)
    UIMenuPanels.createServer.bruteClose()
  }

//  dom.document.getElementById("confirm").asInstanceOf[html.Anchor].onclick = (_: MouseEvent) => {
//    try {
//      val portContent = dom.document.getElementById("inputPort").asInstanceOf[html.Input].valueAsNumber
//
//      if (portContent < 1024 || portContent > 65535)
//        throw IllegalPortChoice(s"Port number should be comprised between 1024 and 65535 (actual: $portContent)")
//
//      dom.document.getElementById("portError").asInstanceOf[html.Div].style.visibility = "hidden"
//      createServer(portContent)
//
//    } catch {
//      case illegalPort: IllegalPortChoice =>
//        val portError = dom.document.getElementById("portError").asInstanceOf[html.Div]
//        portError.style.visibility = "visible"
//        portError.innerHTML = illegalPort.msg
//      case _: Throwable =>
//        val portError = dom.document.getElementById("portError").asInstanceOf[html.Div]
//        portError.style.visibility = "visible"
//        portError.innerHTML = "Malformed port number"
//    }
//  }

}
