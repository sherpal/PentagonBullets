package gamemenusui

import org.scalajs.dom.html
import ui.UIMenuPanel

import scala.scalajs.js
@js.native
trait CreateServerPanel extends UIMenuPanel {

  def port: html.Input = js.native

}
