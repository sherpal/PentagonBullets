package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

final case class DestroyDamageZone(
    actionId: Long,
    time: Long,
    id: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeDamageZone(id, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
