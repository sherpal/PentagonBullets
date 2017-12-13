package networkcom.messages

import physics._

/** Shape translator. */
sealed trait ShapeMessage extends networkcom.Message
object ShapeMessage {
  def shapeToMessage(shape: Shape): ShapeMessage = shape match {
    case shape: Circle => CircleMessage(shape.radius)
    case shape: ConvexPolygon => PolygonMessage(shape.vertices.map(z => Point(z.re, z.im)), convex = true)
    case shape: NonConvexPolygon => PolygonMessage(shape.vertices.map(z => Point(z.re, z.im)), convex = false)
    case _: MonotonePolygon => throw new NotImplementedError("Monotone Polygons are not implemented yet.")
    // because I never use them.
  }

  def messageToShape(message: ShapeMessage): Shape = message match {
    case CircleMessage(radius) => new Circle(radius)
    case PolygonMessage(verticesPoints, isConvex) => Polygon(verticesPoints.map(_.toComplex), isConvex)
  }
}
final case class CircleMessage(radius: Double) extends ShapeMessage
final case class PolygonMessage(vertices: Vector[Point], convex: Boolean) extends ShapeMessage
