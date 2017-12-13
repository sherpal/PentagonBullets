package entities

import physics.Circle

/**
 * A DamageZone hurts player that stands in it.
 *
 * lastTick is the time at which the DamageZone hurt all the players within.
 */
class DamageZone(val id: Long, val lastGrow: Long, val lastTick: Long,
                 val xPos: Double, val yPos: Double, val shape: Circle) extends Body {

  val rotation: Double = 0.0

  def radius: Int = shape.radius.toInt

}


object DamageZone {

  // A DamageZone deals one damage every half second.
  val tickRate: Long = 500
  val damageOnTick: Double = 5

  // A DamageZone starts at radius 5, and its radius increase by 2 every 2 seconds up to 150.
  val startingRadius: Int = 5
  val growingRate: Long = 2000
  val growingValue: Int = 2
  val maxRadius: Int = 150

  // A DamageZone pops every ~ seconds
  val popRate: Long = 7000


  def apply(id: Long, lastGrow: Long, lastTick: Long, xPos: Double, yPos: Double, radius: Double): DamageZone =
    new DamageZone(
      id, lastGrow, lastTick, xPos, yPos, new Circle(radius)
    )

}