package renderer

import globalvariables.{DataStorage, PlayerDataList, VariableStorage}
import org.scalajs.dom
import org.scalajs.dom.html

/**
 * Here we put all the codes that is common to all the menu pages.
 */
object Common {

  if (VariableStorage.retrieveValue("isThereServer").asInstanceOf[String] != null) {
    dom.document.getElementById("isThereServer").asInstanceOf[html.Paragraph].innerHTML =
      "Server port is " + VariableStorage.retrieveValue("isThereServer").asInstanceOf[String]
  }

//  dom.document.getElementById("githubProjectLink").asInstanceOf[html.Image].onclick = (_: Event) => {
//    ElectronShell.openExternal("https://github.com/sherpal/oh-hell-card-game")
//  }

}
