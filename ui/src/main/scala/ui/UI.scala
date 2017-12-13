package ui

import org.scalajs.dom.html

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal


@js.native
@JSGlobal("UI")
object UI extends js.Object {

  val memoryStack: js.Object = js.native

  def showAlertBox(title: String, text: String, callback: js.Function0[Unit] = js.native): Unit = js.native

  def showConfirmBox(title: String, text: String, callback: js.Function1[Boolean, Unit]): Unit = js.native

  def freeze(element: html.Element): Unit = js.native

  def unfreeze(element: html.Element): Unit = js.native

  val playerName: html.Input = js.native

}
