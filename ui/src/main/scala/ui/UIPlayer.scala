package ui

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("UI.Player")
class UIPlayer(val rank: Int, val name: String, val properties: js.Array[UIStatProperty]) extends js.Object {}
