package uielements

import org.scalajs.dom
import org.scalajs.dom.html

object AlertBox extends MessageBox[() => Unit] {

  protected var closeCallback: () => Unit = () => {}

  private val closeButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
  closeButton.textContent = "OK"
  addButton(closeButton)

  closeButton.onclick = (_: dom.MouseEvent) => {
    closeCallback()
    hide()
  }

  def show(title: String, content: String, callback: () => Unit = () => {}): Unit = {
    appear(title, content)
    closeCallback = callback
  }

}
