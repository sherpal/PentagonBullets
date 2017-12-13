package entities

import custommath.Complex

/**
 * A Moving Entity has a speed (which is its norm) and a direction towards which it moves.
 */
trait Moving extends WithPosition {
  val speed: Double
  val direction: Double
  val moving: Boolean

  def currentPosition(time: Long): Complex = if (moving)
    Complex(xPos, yPos) + time * speed * Complex.rotation(direction) / 1000
  else
    Complex(xPos, yPos)
}
