package renderer

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import sharednodejsapis.IPCRenderer

object Tooltip {

  val containerDiv: html.Div = dom.document.getElementById("tooltipContainer").asInstanceOf[html.Div]

  val contentParagraph: html.Paragraph = dom.document.getElementById("tooltipContent").asInstanceOf[html.Paragraph]

  def main(args: Array[String]): Unit = {

    println("hello")

    IPCRenderer.on(
      "tooltip-text",
      (_: Event, text: Any) => {
        println(text)
        try {
          contentParagraph.textContent = text.asInstanceOf[String]
          IPCRenderer.send("tooltip-height", containerDiv.offsetHeight.toInt)

        } catch {
          case e: Throwable =>
            IPCRenderer.send("tooltip-error", e.getStackTrace.mkString("\n").toString)
        }
      }
    )

    IPCRenderer.on("test", (_: Event, counter: Any) => {
      contentParagraph.textContent = counter.toString
    })

  }

}
