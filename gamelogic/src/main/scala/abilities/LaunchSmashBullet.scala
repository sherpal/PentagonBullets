package abilities

import custommath.Complex
import entities.{Entity, SmashBullet}
import gamestate.ActionSource.AbilitySource
import gamestate.{GameAction, GameState}
import gamestate.actions.NewSmashBullet

class LaunchSmashBullet(val time: Long, val useId: Long, val casterId: Long, val startingPos: Complex,
                        val rotation: Double) extends Ability {

  def copyWithUseId(newUseId: Long, newTime: Long): Ability =
    new LaunchSmashBullet(newTime, newUseId, casterId, startingPos, rotation)

  def createActions(gameState: GameState): List[GameAction] = List(
    NewSmashBullet(
      GameAction.newId(), time, Entity.newId(), casterId, startingPos, rotation, SmashBullet.defaultRadius,
      SmashBullet.speed, AbilitySource
    )
  )

  def isLegal(gameState: GameState): Boolean = gameState.isLivingUnitAlive(casterId)

  val cooldown: Long = 15000

  val id: Int = Ability.launchSmashBulletId

}

