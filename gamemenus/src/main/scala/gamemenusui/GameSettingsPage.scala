package gamemenusui

import org.scalajs.dom.html
import ui.UIPage

import scala.scalajs.js

@js.native
trait GameSettingsPage extends UIPage {

  val tablePlayers: html.Table = js.native

  val gameName: html.Head = js.native

  val form: html.Form = js.native

  val ability: html.Select = js.native

  val team: html.Select = js.native

  val ready: html.Input = js.native // check box

}
