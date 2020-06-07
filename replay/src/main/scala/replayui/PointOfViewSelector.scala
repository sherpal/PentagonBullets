package replayui

import gamereconstruction.ReplayGameMode
import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable

class PointOfViewSelector private (val name: String, val replayGameMode: ReplayGameMode) {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.width = "100%"
  PointOfViewSelector.pointOfViewDiv.appendChild(div)

  private val label: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
  label.textContent = name
  div.appendChild(label)

  div.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => {
    PointOfViewSelector.select(this)
  })

}

object PointOfViewSelector {

  private val selectors: mutable.Set[PointOfViewSelector] = mutable.Set()

  private def select(selector: PointOfViewSelector): Unit = {
    selectors.foreach(_.div.className = "")
    selector.div.className = "selectedPointOfView"
    selector.replayGameMode.setPointOfView(selector.name)
  }

  val pointOfViewDiv: html.Div = dom.document.getElementById("pointOfViewDiv").asInstanceOf[html.Div]

  def apply(name: String, replayGameMode: ReplayGameMode): PointOfViewSelector = {
    val selector = new PointOfViewSelector(name, replayGameMode)

    selectors += selector

    selector
  }

}
