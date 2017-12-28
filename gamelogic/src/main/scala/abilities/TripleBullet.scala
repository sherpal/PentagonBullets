package abilities

import custommath.Complex
import entities.{Bullet, Entity}
import gamestate.ActionSource.AbilitySource
import gamestate.{GameAction, GameState}
import gamestate.actions.NewBullet

/**
 * Fires five bullets simultaneously, in the direction of the mouse, spaced from angles -pi/16 to pi/16.
 *
 * Remark: the name does not reflect what it does anymore, but we keep for historical reasons. (Also because I'm lazy.)
 */
class TripleBullet(val time: Long, val useId: Long, val casterId: Long, val teamId: Int, val startingPos: Complex,
                   val rotation: Double) extends Ability {

  def copyWithUseId(newUseId: Long, newTime: Long): Ability = new TripleBullet(
    newTime, newUseId, casterId, teamId, startingPos, rotation
  )

  def createActions(gameState: GameState): List[GameAction] = (0 until TripleBullet.bulletNbr)
    .map(_ * math.Pi / 8 / (TripleBullet.bulletNbr - 1) + - math.Pi / 16)
    .map(alpha => NewBullet(
      GameAction.newId(), Entity.newId(), casterId, teamId, startingPos, Bullet.defaultRadius, rotation + alpha,
      Bullet.speed * 5 / 4, time, 0,
      AbilitySource
    )).toList

  def isLegal(gameState: GameState): Boolean = gameState.isLivingUnitAlive(casterId)

  val cooldown: Long = 10000

  val id: Int = Ability.tripleBulletId

}


object TripleBullet {

  private val bulletNbr: Int = 5

}