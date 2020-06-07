package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * cfr docs: https://github.com/electron/electron/blob/master/docs/api/shell.md
  */
@js.native
@JSImport("electron", "shell")
object ElectronShell extends js.Object {

  def openExternal(link: String): Boolean = js.native

}
