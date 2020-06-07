package ui

import org.scalajs.dom.html

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
@js.native
@JSGlobal("UI.Panel")
abstract class UIPanel extends js.Object {

  val containerElement: html.Element = js.native

  val actionButton: html.Element = js.native

  val slidingElement: html.Element = js.native

  def close(): Unit = js.native

}
