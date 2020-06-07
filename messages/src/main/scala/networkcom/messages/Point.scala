package networkcom.messages

import custommath.Complex

/**
  * Complex translator.
  */
final case class Point(x: Double, y: Double) extends networkcom.Message {
  def toComplex: Complex = Complex(x, y)
}
