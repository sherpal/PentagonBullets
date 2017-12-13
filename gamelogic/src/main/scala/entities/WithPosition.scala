package entities

import custommath.Complex

/**
 * A WithPosition Entity has xPos and yPos values that determines their place in the world.
 */
trait WithPosition extends Entity {
  val xPos: Double
  val yPos: Double
  val pos: Complex = Complex(xPos, yPos)
}
