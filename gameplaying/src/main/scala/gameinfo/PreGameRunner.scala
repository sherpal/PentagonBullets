package gameinfo

import gameengine.Engine
import graphics.{Animation, EntityDrawer, SpinningPentagon}
import time.Time

/**
 * The PreGameRunner displays a spinning pentagon at the center of the screen, and is used as a loading screen.
 */
object PreGameRunner extends gameengine.GameState {

  private val spinningPentagon: SpinningPentagon = new SpinningPentagon
  spinningPentagon.addPentagonToStage(Engine.graphics.mainStage)
  spinningPentagon.run()

  def removePentagon(): Unit =
    spinningPentagon.removePentagonFromStage(Engine.graphics.mainStage)

  val run: Option[() => Unit] = None

  def draw(): Unit = {

    Animation.animate(Time.getTime, EntityDrawer.camera)

  }

  def keyPressed(key: String, keyCode: Int, isRepeat: Boolean): Unit = {}

  def keyReleased(key: String, keyCode: Int): Unit = {}

  def mouseMoved(x: Double, y: Double, dx: Double, dy: Double, button: Int): Unit = {}

  def mouseReleased(x: Double, y: Double, button: Int): Unit = {}

  def mousePressed(x: Double, y: Double, button: Int): Unit = {}

  def mouseWheel(dx: Int, dy: Int, dz: Int): Unit = {}

  def update(dt: Double): Unit = {}

}
