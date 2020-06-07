package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

final case class DestroyBarrier(
    actionId: Long,
    time: Long,
    barrierId: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeBarrier(barrierId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
