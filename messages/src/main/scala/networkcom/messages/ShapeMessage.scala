package networkcom.messages

/** Shape translator. */
sealed trait ShapeMessage extends networkcom.Message

final case class CircleMessage(radius: Double) extends ShapeMessage
final case class PolygonMessage(vertices: Vector[Point], convex: Boolean) extends ShapeMessage
