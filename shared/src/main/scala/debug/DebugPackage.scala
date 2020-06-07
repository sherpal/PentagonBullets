package debug

import org.scalajs.dom
import renderermainprocesscom.OpenDevTools

import scala.scalajs.js.timers.setTimeout

class DebugPackage(fileName: String) {

  var beforeReload: () => Unit = () => {}

  setTimeout(1000) {
    println("[info] debugging tool")
    dom.window.addEventListener[dom.KeyboardEvent](
      "keydown",
      (event: dom.KeyboardEvent) => {
        //println(event.keyCode)
        if (event.ctrlKey && event.keyCode == 68) {
          renderermainprocesscom.Message.sendMessageToMainProcess(OpenDevTools())
        } else if (event.ctrlKey && event.keyCode == 82) {
          beforeReload()
          dom.window.location.href = fileName
        }
      }
    )
  }

}
