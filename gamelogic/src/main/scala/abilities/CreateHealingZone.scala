package abilities

import custommath.Complex
import entities.{Entity, HealingZone}
import gamestate.ActionSource.AbilitySource
import gamestate.actions.NewHealingZone
import gamestate.{GameAction, GameState}

/**
 * Puts a HealingZone for the team at the target position
 */
class CreateHealingZone(val time: Long, val useId: Long, val casterId: Long, val targetPos: Complex) extends Ability {

  val id: Int = Ability.createHealingZoneId

  val cooldown: Long = 30000

  def copyWithUseId(newUseId: Long, newTime: Long): CreateHealingZone = new CreateHealingZone(
    newTime, newUseId, casterId, targetPos
  )

  def isLegal(gameState: GameState): Boolean = gameState.isPlayerAlive(casterId)

  def createActions(gameState: GameState): List[GameAction] = List(
    NewHealingZone(
      GameAction.newId(), time, Entity.newId(), casterId,
      HealingZone.lifeSupply, targetPos.re, targetPos.im, AbilitySource
    )
  )

}
