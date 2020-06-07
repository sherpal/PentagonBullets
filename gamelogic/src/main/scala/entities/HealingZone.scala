package entities

import custommath.Complex
import physics.Circle

/**
  * A HealingZone is attached to a Team, and heal any member of the Team that stands in it.
  *
  * A HealingZone has a maximum amount of life that it can give.
  */
class HealingZone(
    val id: Long,
    val creationTime: Long,
    val ownerId: Long,
    val lastTick: Long,
    val lifeSupply: Double,
    val xPos: Double,
    val yPos: Double,
    val shape: Circle
) extends Body {

  val rotation: Double = 0.0

  def radius: Double = shape.radius

  val ticksRemaining: Int = lifeSupply.toInt / HealingZone.healingOnTick

}

object HealingZone {

  val radius: Double = 40

  val lifeSupply: Int    = 40
  val healingOnTick: Int = 5

  val tickRate: Long = 500

  val lifetime: Long = 60000

  def apply(
      id: Long,
      creationTime: Long,
      ownerId: Long,
      lastTick: Long,
      lifeSupply: Double,
      pos: Complex
  ): HealingZone = new HealingZone(
    id,
    creationTime,
    ownerId,
    lastTick,
    lifeSupply,
    pos.re,
    pos.im,
    new Circle(radius)
  )

  def apply(
      id: Long,
      creationTime: Long,
      ownerId: Long,
      lastTick: Long,
      lifeSupply: Double,
      xPos: Double,
      yPos: Double
  ): HealingZone = new HealingZone(
    id,
    creationTime,
    ownerId,
    lastTick,
    lifeSupply,
    xPos,
    yPos,
    new Circle(radius)
  )

}
