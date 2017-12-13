package uielements

import io.ControlType.{KeyboardType, MouseType}
import io.{ControlBindings, ControlType, KeyBindingsLoader}
import org.scalajs.dom
import org.scalajs.dom.html
import ui.UI

object ControlSettings {

  private val openButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
  openButton.textContent = "Control Settings"
  openButton.onmousedown = (_: dom.MouseEvent) => show()
  openButton.disabled = true
  dom.document.body.appendChild(openButton)


  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.position = "fixed"
  div.style.left = "0px"
  div.style.top = "0px"
  div.style.width = "100%"
  div.style.height = "100%"
  div.style.backgroundColor = "rgba(150,150,150,0.5)"
  div.style.display = "none"
  div.style.textAlign = "center"
  div.style.zIndex = "1"

  dom.document.body.appendChild(div)

  div.onclick = (_: dom.MouseEvent) => {}

  private val messageContentDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  messageContentDiv.style.margin = "auto"
  messageContentDiv.style.backgroundColor = "#FFF"
  messageContentDiv.style.borderRadius = "10px"
  messageContentDiv.style.border = "2px solid black"
  messageContentDiv.style.width = "500px"
  messageContentDiv.style.padding = "20px"
  messageContentDiv.style.verticalAlign = "middle"
  messageContentDiv.style.marginTop = "200px"
  messageContentDiv.style.zIndex = "2"

  div.appendChild(messageContentDiv)

  private val messageTitle: html.Heading = dom.document.createElement("h3").asInstanceOf[html.Heading]
  messageTitle.textContent = "Control Settings"
  messageContentDiv.appendChild(messageTitle)

  private val instructions: html.Paragraph = dom.document.createElement("p").asInstanceOf[html.Paragraph]
  instructions.style.marginRight = "auto"
  instructions.style.marginLeft = "auto"
  instructions.style.marginBottom = "10px"
  private val defaultInstructionText: String = "Click on a control."
  instructions.textContent = defaultInstructionText
  messageContentDiv.appendChild(instructions)

  private val controlSettingsDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  messageContentDiv.appendChild(controlSettingsDiv)

  controlSettingsDiv.style.marginLeft = "auto"
  controlSettingsDiv.style.marginRight = "auto"
  controlSettingsDiv.style.border = "1px solid black"

  private val coveringDiv: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  coveringDiv.style.position = "fixed"
  coveringDiv.style.left = "0px"
  coveringDiv.style.top = "0px"
  coveringDiv.style.width = "100%"
  coveringDiv.style.height = "100%"
  coveringDiv.style.display = "none"
  coveringDiv.style.zIndex = "5"
  coveringDiv.tabIndex = 1
  div.appendChild(coveringDiv)


  private class ControlSelector(
                                 val textContent: String,
                                 _setBinding: (ControlBindings, ControlType, Int, String) => ControlBindings,
                                 private var controlType: ControlType, private var code: Int, key: String
                               ) {

    val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
    div.style.marginLeft = "auto"
    div.style.marginRight = "auto"
    div.style.marginBottom = "5px"
    div.style.marginTop = "5px"

    controlSettingsDiv.appendChild(div)

    val text: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
    text.textContent = textContent
    text.style.marginRight = "10px"
    div.appendChild(text)


    val inputValue: html.Anchor = dom.document.createElement("anchor").asInstanceOf[html.Anchor]
    inputValue.textContent = "Arrow Up"
    inputValue.style.backgroundColor = "#ccc"
    inputValue.style.cursor = "pointer"
    div.appendChild(inputValue)


    private def setKeyboardInputTextContent(key: String): Unit =
      if (key == " ") inputValue.textContent = "Space"
      else if (key.length == 1) inputValue.textContent = key.toUpperCase
      else inputValue.textContent = """[A-Z][a-z]*""".r.findAllIn(key).mkString(" ")

    private def setMouseInputTextContent(button: Int): Unit =
      inputValue.textContent = s"Mouse $button"

    controlType match {
      case MouseType() => setMouseInputTextContent(code)
      case KeyboardType() => setKeyboardInputTextContent(key)
    }

    def changeBinding(ct: ControlType, c: Int, key: String): Unit = ct match {
      case KeyboardType() =>
        code = c
        controlType = KeyboardType()
        setKeyboardInputTextContent(key)
      case MouseType() =>
        code = c
        controlType = MouseType()
        setMouseInputTextContent(code)
    }

    private def activeHandler(event: dom.Event): Boolean = {
      event match {
        case event: dom.MouseEvent =>
          if (bindings.isUsed(MouseType(), event.button)) {
            UI.showAlertBox("Button used", s"Mouse ${event.button} is already bound.")
          } else {
            changeBinding(MouseType(), event.button, "")
          }
        case event: dom.KeyboardEvent =>
          if (bindings.isUsed(KeyboardType(), event.keyCode)) {
            UI.showAlertBox("Key used", s"Key ${event.key} is already bound.")
          } else {
            changeBinding(KeyboardType(), event.keyCode, event.key)
          }
        case _ =>
          dom.console.error("Should not come here")
      }
      coveringDiv.blur()
      coveringDiv.style.display = "none"

      instructions.textContent = defaultInstructionText

      false
    }

    inputValue.onmousedown = (_: dom.MouseEvent) => {
      coveringDiv.style.display = "block"
      coveringDiv.onclick = activeHandler
      coveringDiv.onkeydown = activeHandler
      coveringDiv.focus()

      instructions.textContent = "Press any key or mouse button."

      false
    }

    def setBinding(binding: ControlBindings): ControlBindings =
      _setBinding(binding, controlType, code, inputValue.textContent)

  }

  private object Selectors {
    val up: ControlSelector = new ControlSelector(
      "Going up",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          up = (controlType, code),
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.up._1,
      KeyBindingsLoader.bindings.up._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.up._2, "")
    )

    val down: ControlSelector = new ControlSelector(
      "Going down",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          down = (controlType, code),
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.down._1,
      KeyBindingsLoader.bindings.down._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.down._2, "")
    )

    val left: ControlSelector = new ControlSelector(
      "Going left",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          left = (controlType, code),
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.left._1,
      KeyBindingsLoader.bindings.left._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.left._2, "")
    )

    val right: ControlSelector = new ControlSelector(
      "Going right",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          right = (controlType, code),
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.right._1,
      KeyBindingsLoader.bindings.right._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.right._2, "")
    )

    val bulletShoot: ControlSelector = new ControlSelector(
      "Shoot Bullet",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          bulletShoot = (controlType, code),
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.bulletShoot._1,
      KeyBindingsLoader.bindings.bulletShoot._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.bulletShoot._2, "")
    )

    val selectedAbility: ControlSelector = new ControlSelector(
      "Ability",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          selectedAbility = (controlType, code),
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.selectedAbility._1,
      KeyBindingsLoader.bindings.selectedAbility._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.selectedAbility._2, "")
    )


    val shieldAbility: ControlSelector = new ControlSelector(
      "Shield",
      (binding, controlType, code, keyCode) =>
        binding.copy(
          abilities = (controlType, code) +: binding.abilities.tail,
          keyCodeToKey = if (controlType == KeyboardType())
            binding.keyCodeToKey + (code -> keyCode)
          else
            binding.keyCodeToKey
        ),
      KeyBindingsLoader.bindings.abilities.head._1,
      KeyBindingsLoader.bindings.abilities.head._2,
      KeyBindingsLoader.bindings.keyCodeToKey.getOrElse(KeyBindingsLoader.bindings.abilities.head._2, "")
    )

  }

  private def selectors: List[ControlSelector] = List(
    Selectors.left, Selectors.right, Selectors.up, Selectors.down,
    Selectors.bulletShoot, Selectors.selectedAbility, Selectors.shieldAbility
  )

  private val restoreDefaults: html.Input = dom.document.createElement("input").asInstanceOf[html.Input]
  restoreDefaults.`type` = "button"
  restoreDefaults.value = "Restore Defaults"
  restoreDefaults.onclick = (_: dom.Event) => {
    val default = KeyBindingsLoader.defaultBindings
    Selectors.left.changeBinding(default.left._1, default.left._2, default.keyCodeToKey.getOrElse(default.left._2, ""))
    Selectors.right.changeBinding(
      default.right._1, default.right._2, default.keyCodeToKey.getOrElse(default.right._2, "")
    )
    Selectors.up.changeBinding(default.up._1, default.up._2, default.keyCodeToKey.getOrElse(default.up._2, ""))
    Selectors.down.changeBinding(default.down._1, default.down._2, default.keyCodeToKey.getOrElse(default.down._2, ""))
    Selectors.bulletShoot.changeBinding(
      default.bulletShoot._1, default.bulletShoot._2, default.keyCodeToKey.getOrElse(default.bulletShoot._2, "")
    )
    Selectors.selectedAbility.changeBinding(
      default.selectedAbility._1, default.selectedAbility._2,
      default.keyCodeToKey.getOrElse(default.selectedAbility._2, "")
    )
    Selectors.shieldAbility.changeBinding(
      default.abilities.head._1, default.abilities.head._2,
      default.keyCodeToKey.getOrElse(default.abilities.head._2, "")
    )
  }
  messageContentDiv.appendChild(restoreDefaults)

  private val closeButton: html.Button = dom.document.createElement("button").asInstanceOf[html.Button]
  closeButton.textContent = "Close"
  closeButton.onclick = (_: dom.MouseEvent) => {
    hide()
  }
  messageContentDiv.appendChild(closeButton)


  private def bindings: ControlBindings = selectors.foldLeft(KeyBindingsLoader.bindings)( {
    case (binding, selector) =>
      selector.setBinding(binding)
  })

  private def saveBindings(): Unit = {
    KeyBindingsLoader.saveBindings(bindings, (err) => println(err))
  }

  def show(): Unit = {
    div.style.display = "block"
  }

  def hide(): Unit = {
    div.style.display = "none"
    saveBindings()
  }


  private def load(): Unit = {
    if (KeyBindingsLoader.loaded) {
      Selectors
      openButton.disabled = false
      println("try")
    } else {
      scala.scalajs.js.timers.setTimeout(200) {
        load()
      }
    }
  }

  load()


}
