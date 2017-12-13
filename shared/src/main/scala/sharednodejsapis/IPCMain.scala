package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("electron", "ipcMain")
object IPCMain extends EventEmitter {

}


trait IPCMainEvent extends js.Object {
  val sender: WebContents

  var returnValue: Any
}
