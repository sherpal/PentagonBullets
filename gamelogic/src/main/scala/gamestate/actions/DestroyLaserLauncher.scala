package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

final case class DestroyLaserLauncher(
                                       actionId: Long,
                                       time: Long,
                                       laserLauncherId: Long,
                                       actionSource: ActionSource
                                     ) extends GameAction {

  def setId(newId: Long): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  override def applyDefault(gameState: GameState): GameState = gameState.removeLaserLauncher(laserLauncherId, time)

}
