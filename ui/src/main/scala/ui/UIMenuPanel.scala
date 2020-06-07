package ui

import org.scalajs.dom.html

import scala.scalajs.js

@js.native
trait UIMenuPanel extends UIPanel {

  val formElement: html.Form = js.native

  def bruteClose(): Unit = js.native

}
