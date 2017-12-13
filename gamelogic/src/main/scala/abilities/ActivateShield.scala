package abilities

import entities.{Entity, Shield}
import gamestate.ActionSource.AbilitySource
import gamestate.{GameAction, GameState}
import gamestate.actions.NewActionChanger


class ActivateShield(val time: Long, val useId: Long, val playerId: Long) extends Ability {

  val id: Int = Ability.activateShieldId

  val cooldown: Long = 60000

  val casterId: Long = playerId

  def createActions: List[GameAction] = List(
    NewActionChanger(GameAction.newId(), time, new Shield(Entity.newId(), time, playerId), AbilitySource)
  )

  def copyWithUseId(newUseId: Long, newTime: Long): Ability = new ActivateShield(newTime, newUseId, playerId)

  def isLegal(gameState: GameState): Boolean = gameState.isLivingUnitAlive(playerId)

}

