package entities

import custommath.Complex
import physics.Shape

/**
  * A Body has a [[Shape]] attached to it and can collide other Bodies.
  */
trait Body extends WithPosition {
  val shape: Shape

  val rotation: Double

  def collides(that: Body): Boolean =
    shape.collides(pos, rotation, that.shape, Complex(that.xPos, that.yPos), that.rotation)

  def collides(that: MovingBody, thatTime: Long): Boolean =
    shape.collides(pos, rotation, that.shape, that.currentPosition(thatTime), that.rotation)

  /**
    * Finds the first position, going from pos and towards rotation, where this body does not collide the bodies.
    *
    * @param rotation  The angle towards which we need to go.
    * @param bodies    All the bodies we want to avoid.
    * @param precision Steps by which trying.
    * @return          First position where this body will have no collision.
    */
  def firstValidPosition(rotation: Double, bodies: Iterable[Body], precision: Int = 5): Complex = {
    val dir = Complex.rotation(rotation)

    def tryNextPos(nextPos: Complex): Complex =
      if (!bodies.exists(body => body.shape.collides(body.pos, body.rotation, this.shape, nextPos, this.rotation)))
        nextPos
      else
        tryNextPos(nextPos + dir * precision)

    tryNextPos(pos + precision * dir)
  }

  /**
    * Find the position z closest to targetPosition, on the segment [Complex(xPos, yPos), targetPosition] such that
    * there is no collision on the segment.
    *
    * @param targetPosition The position that we try to reach
    * @param bodies         All the Bodies that can enter in collision
    * @param precision      The step moving forward (typically 5-10 pixels)
    * @return               The final position
    */
  def lastValidPosition(targetPosition: Complex, bodies: Iterable[Body], precision: Int = 5): Complex = {
    val direction = targetPosition - pos
    val dist      = direction.modulus

    if (dist < 2)
      pos
    else {
      val dir = precision * direction / dist

      def tryNextPos(curPos: Complex): Complex = {
        val nextPos = curPos + dir

        if (bodies.exists(
              body => body.shape.collides(Complex(body.xPos, body.yPos), body.rotation, shape, nextPos, rotation)
            )) // if the next position collides, then we should stop here
          curPos
        else if ((nextPos - pos).modulus > dist)
          targetPosition
        else // otherwise, we go further
          tryNextPos(nextPos)

      }

      tryNextPos(pos)
    }
  }

}
