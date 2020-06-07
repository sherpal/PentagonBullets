package gamestate.actions

import custommath.Complex
import entities.HealUnit
import gamestate.{ActionSource, GameAction, GameState}

/**
  * Add a new HealUnit to the game state.
  */
final case class NewHealUnit(
    actionId: Long,
    time: Long,
    id: Long,
    pos: Complex,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState =
    gameState
      .withHealUnit(id, time, new HealUnit(id, time, pos.re, pos.im))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
