package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

final case class DestroySmashBullet(
    actionId: Long,
    time: Long,
    bulletId: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeSmashBullet(bulletId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
