package gamestate.actions

import custommath.Complex
import entities.AbilityGiver
import gamestate.{ActionSource, GameAction, GameState}

final case class NewAbilityGiver(
    actionId: Long,
    time: Long,
    abilityGiverId: Long,
    pos: Complex,
    abilityId: Int,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState =
    gameState
      .withAbilityGiver(abilityGiverId, time, new AbilityGiver(abilityGiverId, time, pos.re, pos.im, abilityId))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
