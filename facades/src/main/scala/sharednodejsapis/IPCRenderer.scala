package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("electron", "ipcRenderer")
object IPCRenderer extends EventEmitter {

  def send(channel: String, args: Any*): Unit = js.native

  def sendSync(channel: String, args: Any*): Any = js.native

}
