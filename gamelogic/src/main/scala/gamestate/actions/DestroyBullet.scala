package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

/**
 * This action happens when a Bullet is destroyed because it ran on more than Bullet.reach pixels.
 */
final case class DestroyBullet(
                                actionId: Long,
                                bulletId: Long,
                                time: Long,
                                actionSource: ActionSource
                              ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeBullet(bulletId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
