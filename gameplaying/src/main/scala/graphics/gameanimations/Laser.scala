package graphics.gameanimations

import custommath.Complex
import gamestate.GameState
import graphics.{Camera, GameAnimation}
import pixigraphics.{PIXIContainer, PIXIGraphics}

class Laser(z1: Complex, z2: Complex, color: Int, stage: PIXIContainer) extends GameAnimation {

  private val graphics: PIXIGraphics = new PIXIGraphics()

  stage.addChild(graphics)

  val duration: Option[Long] = Some(500)

  def stopRunningCallback(): Unit = {
    stage.removeChild(graphics)
  }

  protected def animate(gameState: GameState, now: Long, camera: Camera): Unit = {
    val localZ1 = camera.worldToLocal(z1)
    val localZ2 = camera.worldToLocal(z2)
    graphics
      .clear()
      .beginFill(color)
      .lineStyle(5, color)
      .moveTo(localZ1._1, localZ1._2)
      .lineTo(localZ2._1, localZ2._2)
      .endFill()
  }


  run()

}
