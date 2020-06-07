package uielements

import org.scalajs.dom
import org.scalajs.dom.html

trait MessageBox[CallbackType] {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.position        = "fixed"
  div.style.left            = "0px"
  div.style.top             = "0px"
  div.style.width           = "100%"
  div.style.height          = "100%"
  div.style.backgroundColor = "rgba(150,150,150,0.5)"
  div.style.display         = "none"
  div.style.textAlign       = "center"
  div.style.zIndex          = "3"

  dom.document.body.appendChild(div)

  div.onclick = (_: dom.MouseEvent) => {}

  private val messageContentDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  messageContentDiv.style.margin          = "auto"
  messageContentDiv.style.backgroundColor = "#FFF"
  messageContentDiv.style.borderRadius    = "10px"
  messageContentDiv.style.border          = "2px solid black"
  messageContentDiv.style.width           = "300px"
  messageContentDiv.style.padding         = "20px"
  messageContentDiv.style.verticalAlign   = "middle"
  messageContentDiv.style.marginTop       = "200px"
  messageContentDiv.style.zIndex          = "4"

  div.appendChild(messageContentDiv)

  private val messageTitle: html.Heading = dom.document.createElement("h1").asInstanceOf[html.Heading]
  messageContentDiv.appendChild(messageTitle)

  private val messageContent: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
  messageContentDiv.appendChild(messageContent)

  private val buttonDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  messageContentDiv.appendChild(buttonDiv)

  protected def addButton(button: html.Button): Unit =
    buttonDiv.appendChild(button)

  protected def appear(title: String, content: String): Unit = {
    messageTitle.textContent   = title
    messageContent.textContent = content
    div.style.display          = "block"
  }

  protected def hide(): Unit =
    div.style.display = "none"

  def show(title: String, content: String, callback: CallbackType): Unit

  protected var closeCallback: CallbackType

  def apply(title: String, content: String, callback: CallbackType): Unit = show(title, content, callback)

}
