package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

/**
  * Removes a [[entities.BulletAmplifier]] from the game.
  */
final case class DestroyBulletAmplifier(
    actionId: Long,
    time: Long,
    bulletAmplifierId: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeBulletAmplifier(bulletAmplifierId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
