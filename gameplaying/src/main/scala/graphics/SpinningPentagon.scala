package graphics

import custommath.Complex
import physics.BoundingBox
import pixigraphics.{PIXIContainer, PIXITexture, Sprite}

class SpinningPentagon extends Animation {

  private val pentagon: Sprite = new Sprite(PIXITexture.fromImage("../../assets/icon/iconpng.png"))

  def addPentagonToStage(stage: PIXIContainer): Unit =
    stage.addChild(pentagon)

  def removePentagonFromStage(stage: PIXIContainer): Unit =
    stage.removeChild(pentagon)

  val duration: Option[Long] = None

  pentagon.anchor.set(0.5, 0.5)

  def animate(currentTime: Long, camera: Camera): Unit = {

    pentagon.rotation = (currentTime - startTime).toDouble / 1000 * math.Pi
    camera.viewportManager(pentagon, Complex(0,0), new BoundingBox(-10, -10, 10, 10))
    pentagon.width = 30.0
    pentagon.height = 30.0

  }

}
