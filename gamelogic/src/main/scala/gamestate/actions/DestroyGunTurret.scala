package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

final case class DestroyGunTurret(
                                   actionId: Long,
                                   time: Long,
                                   turretId: Long,
                                   actionSource: ActionSource
                                 ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.removeGunTurret(turretId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
