package gamemenusui

import org.scalajs.dom.html

import scala.scalajs.js
@js.native
trait HostPanel extends PlayerConnectionInfoPanel {

  def mode: html.Select = js.native

}
