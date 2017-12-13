package abilities

import custommath.Complex
import entities.{Bullet, Entity}
import gamestate.ActionSource.AbilitySource
import gamestate.{GameAction, GameState}
import gamestate.actions.NewBullet

/**
 * Launch a BigBullet that is three times as big, and deals three times as much damage.
 */
class BigBullet(val time: Long, val useId: Long, val casterId: Long, val teamId: Int, val startingPos: Complex,
                val rotation: Double) extends Ability {

  def copyWithUseId(newUseId: Long, newTime: Long): Ability =
    new BigBullet(newTime, newUseId, casterId, teamId, startingPos, rotation)

  def createActions: List[GameAction] = List(
    NewBullet(
      GameAction.newId(),
      Entity.newId(), casterId, teamId, startingPos, 3 * Bullet.defaultRadius, rotation, Bullet.speed * 2, time, 0,
      AbilitySource
    )
  )

  def isLegal(gameState: GameState): Boolean = gameState.isLivingUnitAlive(casterId)

  val cooldown: Long = 10000

  val id: Int = Ability.bigBulletId

}
