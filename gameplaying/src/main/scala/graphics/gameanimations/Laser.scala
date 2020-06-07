package graphics.gameanimations

import custommath.Complex
import gamestate.GameState
import graphics.{Camera, GameAnimation}
import pixigraphics.{PIXIContainer, PIXIGraphics}

import scala.scalajs.js.JSConverters._

class Laser(vertices: Vector[Complex], color: Int, stage: PIXIContainer) extends GameAnimation {

  private val graphics: PIXIGraphics = new PIXIGraphics()

  stage.addChild(graphics)

  val duration: Option[Long] = Some(600)

  def stopRunningCallback(): Unit =
    stage.removeChild(graphics)

  private val cycleVertices: Vector[Complex] = vertices.last +: vertices

  protected def animate(gameState: GameState, now: Long, camera: Camera): Unit = {
    val localCoords = cycleVertices
      .map(camera.worldToLocal)
      .flatMap(elem => Vector(elem._1, elem._2))
      .toJSArray

    graphics
      .clear()
      .beginFill(color)
      .drawPolygon(localCoords)
      .endFill()
  }

  run()

}
