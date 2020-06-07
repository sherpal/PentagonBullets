package plots.plotelements

import custommath.Complex
import plots.{Plot, PlotElement}
import webglgraphics.Vec4
import scala.Ordering.Double.TotalOrdering

class Rectangle(x: Double, y: Double, width: Double, height: Double, color: Vec4) extends Region {

  override val zIndex: Int = -1

  private val vertices: Vector[Complex] = Vector(
    Complex(x, y),
    Complex(x, y - height),
    Complex(x + width, y - height),
    Complex(x + width, y)
  )

  private val edges: Vector[(Complex, Complex)] =
    vertices.zip(vertices.last +: vertices.dropRight(1))

  def draw(plot: Plot): PlotElement = {
    val canvasCoords = plot.plotToCanvasCoordinates(x, y)

    plot.fillRect(canvasCoords._1, canvasCoords._2, width * plot.canvasXUnit, height * plot.canvasYUnit, color)

    this
  }

  def closestPointTo(z: Complex): Complex =
    edges.map({ case (z1, z2) => PlotElement.closestPointToInterval(z, z1, z2) }).minBy(w => (z - w).modulus2)

  override def closestPointToCanvasCoords(z: Complex, plot: Plot): Complex =
    edges
      .map({
        case (z1, z2) =>
          (Complex(plot.plotToCanvasCoordinates(z1.re, z1.im)), Complex(plot.plotToCanvasCoordinates(z2.re, z2.im)))
      })
      .map({ case (z1, z2) => PlotElement.closestPointToInterval(z, z1, z2) })
      .minBy(w => (z - w).modulus2)

}
