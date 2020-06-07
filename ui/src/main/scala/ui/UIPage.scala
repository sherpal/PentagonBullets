package ui

import org.scalajs.dom.html

import scala.scalajs.js

@js.native
trait UIPage extends js.Object {

  val containerElement: html.Element = js.native

  val quitButton: html.Input = js.native

  def open(): Unit = js.native

}
