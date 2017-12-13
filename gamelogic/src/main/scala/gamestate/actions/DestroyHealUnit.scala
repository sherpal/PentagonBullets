package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

/**
 * Removes a HealUnit from the game state.
 */
final case class DestroyHealUnit(
                                  actionId: Long,
                                  time: Long,
                                  id: Long,
                                  actionSource: ActionSource
                                ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeHealUnit(id, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
