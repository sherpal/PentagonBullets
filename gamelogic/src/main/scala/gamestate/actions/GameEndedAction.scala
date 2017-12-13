package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

final case class GameEndedAction(
                                  actionId: Long,
                                  time: Long,
                                  actionSource: ActionSource
                                ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.ends(time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
