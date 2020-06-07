package plots

import custommath.Complex
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, html}
import webglgraphics.{Vec3, Vec4}


import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.Ordering.Double.TotalOrdering

class Plot protected {

  private val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  private val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  def canvasElement: html.Canvas = canvas

  def setSize(width: Int, height: Int): Unit = {
    canvas.width = width
    canvas.height = height
  }

  def width: Int = canvas.width
  def height: Int = canvas.height

  def clear(): Plot = {
    ctx.clearRect(0, 0, width, height)
    ctx.fillStyle = backgroundColor.toCSSColor
    ctx.fillRect(0, 0, width, height)

    this
  }

  private var backgroundColor: Vec4 = Vec4(0.7, 0.7, 0.7, 1)

  def setBackgroundColor(red: Double, green: Double, blue: Double, alpha: Double = 1.0): Plot = {
    backgroundColor = Vec4(red, green, blue, alpha)

    this
  }

  private var minXAxis: Double = -2
  private var maxXAxis: Double = 2

  private var minYAxis: Double = -2
  private var maxYAxis: Double = 2

  def setXAxis(min: Double, max: Double): Plot = {
    minXAxis = min
    maxXAxis = max

    this
  }

  def xAxis: (Double, Double) = (minXAxis, maxXAxis)

  def setYAxis(min: Double, max: Double): Plot = {
    minYAxis = min
    maxYAxis = max

    this
  }

  def yAxis: (Double, Double) = (minYAxis, maxYAxis)

  def canvasXUnit: Double = width / (maxXAxis - minXAxis)

  def canvasYUnit: Double = height / (maxYAxis - minYAxis)

  def plotToCanvasCoordinates(x: Double, y: Double): (Double, Double) = {
    (
      (x - minXAxis) * canvasXUnit,
      height - (y - minYAxis) * canvasYUnit
    )
  }

  def canvasToPlotCoordinates(x: Double, y: Double): (Double, Double) = {
    (
      minXAxis + x / width * (maxXAxis - minXAxis),
      maxYAxis - y / height * (maxYAxis - minYAxis)
    )
  }

  def drawAxes(): Plot = {
    if (minYAxis * maxYAxis <= 0) { // 0 \in [minYAxis, maxYAxis]
      drawLine(Vector(minXAxis, maxXAxis), Vector(0, 0), Vec3(0.5, 0.5, 0.5))
    }

    if (minXAxis * maxXAxis <= 0) { // 0 \in [minxAxis, maxXAxis]
      drawLine(Vector(0, 0), Vector(minYAxis, maxYAxis), Vec3(0.5, 0.5, 0.5))
    }

    this
  }

  def write(
             text: String, x: Double, y: Double,
             color: Vec4 = Vec4(0,0,0,1),
             maxWidth: Option[Double] = None,
             fontName: String = "quicksand",
             fontSize: Int = 20
           ): Plot = {
    ctx.fillStyle = color.toCSSColor
    ctx.font = s"${fontSize}px $fontName"

    if (maxWidth.isDefined) {
      ctx.fillText(text, x, y, maxWidth.get)
    } else {
      ctx.fillText(text, x, y)
    }

    this
  }

  def drawLine(
                xs: Vector[Double], ys: Vector[Double],
                color: Vec4 = Vec3(0,0,0),
                dashed: Option[Seq[Double]] = None
              ): Plot = {

    ctx.strokeStyle = color.toCSSColor

    ctx.beginPath()

    if (dashed.isDefined) {
      ctx.setLineDash(dashed.get.toJSArray)
    }

    val plotCoords = xs.zip(ys).map({ case (x, y) => plotToCanvasCoordinates(x, y)})

    ctx.moveTo(plotCoords(0)._1, plotCoords(0)._2)

    plotCoords
      .tail
      .foreach({ case (x, y) => ctx.lineTo(x, y) })

    ctx.stroke()

    if (dashed.isDefined) {
      ctx.setLineDash(js.Array[Double]())
    }

    this
  }

  private val unitCircleLength: Double = 2 * math.Pi

  def drawPoint(x: Double, y: Double, radius: Double, color: Vec4 = Vec4(0, 0, 0, 1)): Plot = {

    ctx.fillStyle = color.toCSSColor

    ctx.beginPath()

    ctx.arc(x, y, radius, 0, unitCircleLength)

    ctx.closePath()

    ctx.fill()

    this
  }

  def fillRect(x: Double, y: Double, width: Double, height: Double, color: Vec4): Plot = {
    ctx.fillStyle = color.toCSSColor
    ctx.fillRect(x, y, width, height)

    this
  }


  protected var children: List[PlotElement] = Nil

  def addChild(child: PlotElement): Plot = {
    children = (child +: children).sortBy(_.zIndex)

    this
  }

  def drawChildren(): Plot = {
    children.foreach(_.draw(this))

    this
  }

  def onMouseMove(x: Double, y: Double): Unit = {
    children.foreach(_.onMouseMove(x, y))
  }

  def closestChildToCanvasCoords(
                                  x: Double, y: Double,
                                  childrenFilter: (PlotElement) => Boolean = (_) => true
                                ): Option[PlotElement] =
    if (!children.exists(childrenFilter)) None
    else Some(
      children
        .filter(childrenFilter)
        .minBy(_.distanceToPosCanvasCoords(x, y, this))
    )

  def onMouseMoveCanvasCoords(x: Double, y: Double): Unit = {
    closestChildToCanvasCoords(x, y) match {
      case Some(child) =>
        val z = child.closestPointToCanvasCoords(Complex(x, y), this)

        clear()
        drawAxes()
        drawChildren()
        drawPoint(z.re, z.im, 5)
      case _ =>
    }

  }

  canvas.addEventListener[dom.MouseEvent]("mousemove", (event: dom.MouseEvent) => {
    val boundingRect = canvas.getBoundingClientRect()
    val canvasX = event.clientX - boundingRect.left
    val canvasY = event.clientY - boundingRect.top
    val (x, y) = canvasToPlotCoordinates(canvasX, canvasY)

    onMouseMoveCanvasCoords(canvasX, canvasY)
    onMouseMove(x, y)
  })

  def closestChildTo(x: Double, y: Double): PlotElement = children.minBy(_.distanceToPos(x, y))


}


object Plot {

  def apply(
             width: Int = 300, height: Int = 300
           ): Plot = {
    val plot = new Plot

    plot.setSize(width, height)

    plot
  }

  def linSpace(min: Double, max: Double, nbrOfPoints: Int = 100): Vector[Double] =
    (0 until nbrOfPoints).map(min + _ * (max - min) / (nbrOfPoints - 1)).toVector

}
