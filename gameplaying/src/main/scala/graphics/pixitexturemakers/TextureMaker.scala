package graphics.pixitexturemakers

import pixigraphics.PIXITexture

import scala.collection.mutable

trait TextureMaker {

  protected val textures: mutable.Map[(Double, Double, Double), PIXITexture] = mutable.Map()

  def apply(color: (Double, Double, Double)): PIXITexture

}
