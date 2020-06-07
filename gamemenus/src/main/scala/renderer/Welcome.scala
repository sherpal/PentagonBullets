package renderer

import globalvariables.VariableStorage
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import sharednodejsapis.ElectronShell

/**
  * Manages what happens in the main screen html file.
  */
object Welcome {

  if (VariableStorage.retrieveValue("gameCanceled") != null) {
    dom.window.alert(VariableStorage.retrieveValue("gameCanceled").asInstanceOf[String])
    VariableStorage.unStoreValue("gameCanceled")
  }

  dom.document.getElementById("githubTextProjectLink").asInstanceOf[html.Anchor].onclick = (_: Event) => {
    ElectronShell.openExternal("https://github.com/sherpal/oh-hell-card-game")
  }

}
