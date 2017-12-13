package entities

import abilities.Ability
import physics.{Polygon, Shape}

/**
 * A Player represents a Human player.
 */
class Player(val id: Long, val team: Int, val time: Long, val name: String,
             val xPos: Double = 0, val yPos: Double = 0,
             val direction: Double = 0, val speed: Double = Player.speed, val moving: Boolean = false,
             val rotation: Double = 0, val shape: Polygon = Player.shape,
             val lifeTotal: Double = 100,
             val allowedAbilities: List[Int],
             val relevantUsedAbilities: Map[Long, Ability]) extends MovingBody with Living with WithAbilities {


}

object Player {
  val radius: Double = 20

  val speed: Double = 200

  val shape: Polygon = Shape.regularPolygon(5, radius)

  val maxLife: Double = 100

  val maxBulletRate: Long = 100

  def bulletHitPlayer(player: Player, damage: Double, time: Long): Player = new Player(
    player.id, player.team, player.time, player.name, player.xPos, player.yPos, player.direction,
    moving = player.moving, rotation = player.rotation, shape = player.shape,
    lifeTotal = player.lifeTotal - damage, allowedAbilities = player.allowedAbilities,
    relevantUsedAbilities = player.relevantUsedAbilities
  )

  def smashBulletHitPlayer(player: Player, time: Long): Player = new Player(
    player.id, player.team, player.time, player.name, player.xPos, player.yPos, player.direction,
    player.speed, player.moving, player.rotation, player.shape, math.ceil(player.lifeTotal / 2),
    player.allowedAbilities, player.relevantUsedAbilities
  )

}