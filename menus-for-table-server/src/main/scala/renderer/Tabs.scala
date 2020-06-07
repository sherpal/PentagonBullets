package renderer

import org.scalajs.dom
import org.scalajs.dom.html

object Tabs {

  type Tab = html.Div

  val tableListTab: Tab = dom.document.getElementById("tableListTab").asInstanceOf[html.Div]

  val gameSettingsTab: Tab = dom.document.getElementById("gameSettingsTab").asInstanceOf[html.Div]

  private val tabs: List[Tab] = List(
    tableListTab,
    gameSettingsTab
  )

  def switchTab(tabToOpen: Tab): Unit = {
    tabs.foreach(_.style.display = "none")
    tabToOpen.style.display = "block"
  }

}
