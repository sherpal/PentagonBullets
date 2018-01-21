package plots

import custommath.Complex

trait PlotElement {

  def draw(plot: Plot): PlotElement

  /**
   * Returns the distance between this plot element and the position (x,y), usually mouse position.
   */
  def distanceToPos(x: Double, y: Double): Double =
    (Complex(x, y) - closestPointTo(Complex(x, y))).modulus

  /**
   * Finds the point on the PlotElement the closest to z.
   */
  def closestPointTo(z: Complex): Complex

  /**
   * Same as distanceToPos, but in canvas coordinates. (x,y) must be in canvas coords as well.
   */
  def distanceToPosCanvasCoords(x: Double, y: Double, plot: Plot): Double =
    (Complex(x, y) - closestPointToCanvasCoords(Complex(x, y), plot)).modulus

  /**
   * This does the same as closestPointTo, except distances are looked at in drawing coordinates in the canvas.
   * z must be in canvas coords as well.
   */
  def closestPointToCanvasCoords(z: Complex, plot: Plot): Complex

  /**
   * Called on every PlotElement when the mouse moves on the parent plot.
   */
  def onMouseMove(x: Double, y: Double): Unit = {}

  val zIndex: Int = 0

}


object PlotElement {

  def closestPointToInterval(point: Complex, edge1: Complex, edge2: Complex): Complex = {
    val z = point - edge1
    val w = edge2 - edge1

    val scalarProduct = z scalarProduct (w / w.modulus)

    if (scalarProduct < 0) edge1
    else if (scalarProduct > w.modulus) edge2
    else w / w.modulus * scalarProduct + edge1
  }

}