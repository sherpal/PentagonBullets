package popups

import org.scalajs.dom
import org.scalajs.dom.html

object Alert {

  private val div: html.Div = dom.document.getElementById("alertBox").asInstanceOf[html.Div]

  private val contentDiv: html.Div = div.firstElementChild.asInstanceOf[html.Div]

  private val title: html.Heading = contentDiv.firstElementChild.asInstanceOf[html.Heading]

  private val messageContent: html.Paragraph = title.nextElementSibling.asInstanceOf[html.Paragraph]

  private val closeButton: html.Button = messageContent.nextElementSibling.asInstanceOf[html.Button]

  closeButton.addEventListener("click", (_: dom.Event) => div.style.display = "none")

  def showAlert(titleStr: String, message: String): Unit = {
    div.style.display = "block"

    title.textContent = titleStr

    messageContent.textContent = message
  }

}
