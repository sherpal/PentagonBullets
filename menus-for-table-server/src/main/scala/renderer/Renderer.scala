package renderer

import communication.TableClient
import debug.DebugPackage
import org.scalajs.dom
import org.scalajs.dom.html

object Renderer {

  def main(args: Array[String]): Unit = {

    println("Menus for connecting to the table server.")

    if (scala.scalajs.LinkingInfo.developmentMode) {
      val debug = new DebugPackage("../html/table-menus.html")
      debug.beforeReload = () => {
        if (TableClient.isConnected) {
          TableClient.disconnect()
        }
      }

      val disconnectButton = dom.document.getElementById("disconnect").asInstanceOf[html.Button]
      disconnectButton.addEventListener("click", (_: dom.Event) => TableClient.disconnect())
      disconnectButton.style.display = "block"
    }

    TableClient.connect()

  }

}
