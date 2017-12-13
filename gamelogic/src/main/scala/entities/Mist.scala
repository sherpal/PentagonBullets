package entities

import custommath.Complex
import physics.Polygon

/**
 * A (the?) Mist is similar to a DamageZone, but its purpose and shape is different.
 *
 * The Mist is a neighbourhood of the boundary of the game, and progressively grows until only the two players square is
 * not covered..
 * This forces players to center as the game continues.
 *
 *
 * At each tick, every player in the Mist takes some damage.
 */
class Mist(val id: Long, val lastGrow: Long, val lastTick: Long, val shape: Polygon) extends Body {

  val rotation: Double = 0.0

  val xPos: Double = 0.0
  val yPos: Double = 0.0

  val sideLength: Double = shape.vertices(0).im - shape.vertices(1).im

  def shrink(time: Long, gameAreaSideLength: Int): Double =
      math.max(Mist.minGameSide, sideLength + Mist.sideShrinkingValue * (time - lastGrow).toInt)

}


object Mist {

  val damagePerTick: Int = 5
  val tickRate: Long = 500

  val growthRate: Long = 2000
  private var _sideShrinkingValue: Double = 1.0
  def sideShrinkingValue: Double = _sideShrinkingValue

  private var _growthDuration: Double = 2 * 60000.0
  def growthDuration: Double = _growthDuration

  val minGameSide: Int = 1000

  /**
   * Sets the value of _sideShrinking.
   * @param maxGameSide size of a side of the GameArea at the beginning of the game. This is also the size of the mist
   *                    at the beginning
   * @param nbrPlayers  number of players in the game
   */
  def setSideShrinkingValue(maxGameSide: Int, nbrPlayers: Int): Unit = {
    _growthDuration = math.max(nbrPlayers - 2, 2) * 60000.0

    _sideShrinkingValue = (minGameSide - maxGameSide) / growthDuration
  }


  def makeMistShape(sideLength: Double, gameAreaSideLength: Int): Polygon = {
    Polygon(Vector(
      Complex(sideLength / 2, sideLength / 2),
      Complex(sideLength / 2, -sideLength / 2),
      Complex(gameAreaSideLength / 2, -gameAreaSideLength / 2),
      Complex(gameAreaSideLength / 2, gameAreaSideLength / 2),
      Complex(-gameAreaSideLength / 2, gameAreaSideLength / 2),
      Complex(-gameAreaSideLength / 2, -gameAreaSideLength / 2),
      Complex(gameAreaSideLength / 2, -gameAreaSideLength / 2),
      Complex(sideLength / 2, -sideLength / 2),
      Complex(-sideLength / 2, -sideLength / 2),
      Complex(-sideLength / 2, sideLength / 2)
    ))
  }


  /**
   * Compute the value of the mist side length when current time is time.
   *
   * The function used is currently quadratic. If L is the total size, l the minimum size, and M the duration until it
   * reaches the full size, the length is given by
   * f(t) = (l - L) * t^2 / M^2 + L
   */
  def shrinkFunction(time: Long, gameStartTime: Long, gameAreaSideLength: Double): Double = {
    val dt = time - gameStartTime
    math.max(
      (minGameSide - gameAreaSideLength) / growthDuration * dt * dt / growthDuration + gameAreaSideLength,
      minGameSide
    )
  }


}