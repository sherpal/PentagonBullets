package graphics.pixitexturemakers
import entities.LaserLauncher
import gameengine.Engine
import pixigraphics.{PIXIGraphics, PIXITexture}
import webglgraphics.Vec3

object LaserLauncherTextureMaker extends TextureMaker {

  def apply(color: (Double, Double, Double)): PIXITexture = textures.get(color) match {
    case Some(tex) =>
      tex
    case None =>
      val radius   = LaserLauncher.laserLauncherShapeRadius
      val (x, y)   = (2 * radius, 2 * radius)
      val intColor = Vec3(color._1, color._2, color._3).toInt
      val graphics = new PIXIGraphics()
        .beginFill(0x999999, 0.5)
        .drawRoundedRect(radius, radius, 2 * radius, 2 * radius, radius / 5)
        .endFill()
        .beginFill(intColor)
        .drawCircle(x, y, radius / 3)
        .endFill()
        .lineStyle(radius / 5, intColor)
        .drawRoundedRect(radius, radius, 2 * radius, 2 * radius, radius / 5)
      val tex = Engine.graphics.webGLRenderer.generateTexture(graphics)
      textures += color -> tex
      tex
  }

}
