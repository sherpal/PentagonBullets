package plots.plotelements

import custommath.Complex
import plots.{Plot, PlotElement}
import webglgraphics.Vec4

class Line(xs: Vector[Double], ys: Vector[Double], color: Vec4 = Vec4(0,0,0,1)) extends PlotElement {

  private val zs: Vector[Complex] = xs.zip(ys).map({ case (x, y) => Complex(x, y) })

  private val segments: Vector[(Complex, Complex)] = zs.zip(zs.tail)

  def draw(plot: Plot): PlotElement = {
    plot.drawLine(xs, ys, color)

    this
  }

  def closestPointTo(z: Complex): Complex =
    segments
      .map({ case (z1, z2) => PlotElement.closestPointToInterval(z, z1, z2) })
      .minBy(w => (w - z).modulus2)

  def closestPointToCanvasCoords(z: Complex, plot: Plot): Complex = {
    val canvasCoords = zs.map(z => plot.plotToCanvasCoordinates(z.re, z.im)).map(elem => Complex(elem._1, elem._2))
    canvasCoords.zip(canvasCoords.tail)
      .map({ case (z1, z2) => PlotElement.closestPointToInterval(z, z1, z2) })
      .minBy(w => (w - z).modulus2)
  }

}
