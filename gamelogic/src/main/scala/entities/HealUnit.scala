package entities

import physics.{Circle, Shape}

/**
 * A HealUnit pops at random places during the Game and heal players for some small amount.
 */
class HealUnit(val id: Long, val time: Long, val xPos: Double, val yPos: Double) extends Body {

  val shape: Shape = new Circle(HealUnit.radius)

  val rotation: Double = 0.0

}

object HealUnit {
  val radius: Double = 10

  val lifeTime: Long = 15000 // 15 seconds before disappearing

  val lifeGain: Double = 15 // restore 15 life points when a player takes it.

  def playerTakeUnit(player: Player, time: Long): Player = {
    new Player(
      player.id, player.team, player.time, player.name, player.xPos, player.yPos,
      player.direction, player.speed, player.moving, player.rotation,
      player.shape, math.min(player.lifeTotal + lifeGain, Player.maxLife),
      player.allowedAbilities, player.relevantUsedAbilities
    )
  }
}
