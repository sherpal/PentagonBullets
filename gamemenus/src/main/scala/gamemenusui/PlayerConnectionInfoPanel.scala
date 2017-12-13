package gamemenusui

import org.scalajs.dom.html
import ui.UIMenuPanel

import scala.scalajs.js

@js.native
trait PlayerConnectionInfoPanel extends UIMenuPanel {

  def gameName: html.Input = js.native

  def address: html.Input = js.native

  def port: html.Input = js.native

}
