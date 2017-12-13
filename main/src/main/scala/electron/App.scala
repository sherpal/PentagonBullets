package electron

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


/**
 * The App object is available in the Main Process, and has lots of events that trigger with app related info.
 * See full doc here: https://github.com/electron/electron/blob/master/docs/api/app.md
 */
@js.native
@JSImport("electron", "app")
object App extends js.Object {
  def on(callbackName: String, handler: js.Function): Unit = js.native

  def quit(): Unit = js.native
}



trait AppEvent extends js.Object {



}