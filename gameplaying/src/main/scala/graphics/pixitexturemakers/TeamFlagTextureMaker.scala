package graphics.pixitexturemakers

import gameengine.Engine
import pixigraphics.{PIXITexture, Sprite}
import webglgraphics.Vec3

object TeamFlagTextureMaker extends TextureMaker {

  val flagTexture: PIXITexture = PIXITexture.fromImage("../../assets/entities/team_flag.png")

  def apply(color: (Double, Double, Double)): PIXITexture = textures.get(color) match {
    case Some(tex) =>
      tex
    case None =>
      val sprite = new Sprite(flagTexture)
      sprite.tint = Vec3(color._1, color._2, color._3).toInt
      val colouredTexture = Engine.graphics.webGLRenderer.generateTexture(sprite)
      textures += color -> colouredTexture
      colouredTexture
  }

}
