package graphics.pixitexturemakers

import gameengine.Engine
import pixigraphics.{PIXIGraphics, PIXITexture}
import webglgraphics.Vec3

import scala.collection.mutable


object GunTurretTextureMaker {

  private val textures: mutable.Map[(Double, Double, Double), PIXITexture] = mutable.Map()

  def apply(color: (Double, Double, Double), radius: Double): PIXITexture = textures.get(color) match {
    case Some(tex) =>
      tex
    case None =>
      val (x, y) = (2 * radius, 2 * radius)
      val graphics = new PIXIGraphics()
        .beginFill(Vec3(color._1, color._2, color._3).toInt)
        .drawCircle(x, y, radius)
        .endFill()
        .beginFill(0xFFFFFF, 0.8)
        .drawCircle(x, y, radius / 3)
        .endFill()
        .beginFill(0xFFFFFF)
        .drawRect(x, y - 2, radius * 5 / 4, 4)
        .endFill()
      val tex = Engine.graphics.webGLRenderer.generateTexture(graphics)
      textures += color -> tex
      tex
  }

}
