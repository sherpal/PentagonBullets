package io

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

object KeyBindingsLoader {

  import io.ControlType._

  val defaultBindings: ControlBindings = ControlBindings(
    (KeyboardType(), 90), (KeyboardType(), 83), (KeyboardType(), 81), (KeyboardType(), 68),
    (MouseType(), 0), (MouseType(), 2),
    List((KeyboardType(), 69), (KeyboardType(), -1), (KeyboardType(), 65)),
    Map(
      90 -> "Z", 83 -> "S", 81 -> "Q", 68 -> "D", 69 -> "E", 65 -> "A", -1 -> "1"
    )
  )

  private var _bindings: ControlBindings = defaultBindings
  def bindings: ControlBindings = _bindings

  private var _loaded: Boolean = false
  def loaded: Boolean = _loaded


  private val fileName: String = "bindings1.sav"

  IO.open(s"/saved/$fileName").onComplete({
    case Success(fd) =>
      IO.close(fd)
      IO.readFileContent(s"/saved/$fileName").onComplete({
        case Success(content) =>
          content match {
            case content: ControlBindings =>
              _bindings = content
              println("Bindings loaded")
            case _ =>
              _bindings = defaultBindings
              println("no bindings found, using default")
          }
          _loaded = true
        case _ =>
          _bindings = defaultBindings
          println("no bindings found, using default")
          _loaded = true
      })
    case _ =>
      _bindings = defaultBindings
      println("no bindings found, using default")
      _loaded = true
  })

  def saveBindings(bindings: ControlBindings, errorCallback: (Throwable) => Unit): Unit = {
    if (bindings == defaultBindings) {
      IO.delete(s"/saved/$fileName")
    } else {
      IO.writeFileContent(s"/saved/$fileName", bindings, errorCallback)
    }
  }

}
