package plots.plotelements

import custommath.Complex
import plots.{Plot, PlotElement}
import webglgraphics.Vec4

class Segment(z1: Complex, z2: Complex, color: Vec4 = Vec4(0,0,0,1)) extends PlotElement {

  def draw(plot: Plot): PlotElement = {
    plot.drawLine(Vector(z1.re, z2.re), Vector(z1.im, z2.im), color.toVec3)

    this
  }


  def closestPointTo(z: Complex): Complex = PlotElement.closestPointToInterval(z, z1, z2)

  def closestPointToCanvasCoords(z: Complex, plot: Plot): Complex = {
    val e1 = plot.plotToCanvasCoordinates(z1.re, z1.im)
    val e2 = plot.plotToCanvasCoordinates(z2.re, z2.im)

    PlotElement.closestPointToInterval(z, Complex(e1._1, e1._2), Complex(e2._1, e2._2))
  }

}

