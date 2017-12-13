package abilities

import custommath.Complex
import entities.Entity
import gamestate.ActionSource.AbilitySource
import gamestate.{GameAction, GameState}
import gamestate.actions.NewBulletAmplifier

/**
 * Create a [[entities.BulletAmplifier]] for the caster's team.
 */
class CreateBulletAmplifier(val time: Long, val useId: Long,
                            val casterId: Long, val targetPos: Complex, val rotation: Double) extends Ability {

  val id: Int = Ability.createBulletAmplifierId

  val cooldown: Long = 15000

  def copyWithUseId(newUseId: Long, newTime: Long): CreateBulletAmplifier = new CreateBulletAmplifier(
    newTime, newUseId, casterId, targetPos, rotation
  )

  def createActions: List[GameAction] = List(
    NewBulletAmplifier(GameAction.newId(), time, Entity.newId(), casterId, rotation, targetPos, AbilitySource)
  )

  def isLegal(gameState: GameState): Boolean = gameState.isPlayerAlive(casterId)

}
