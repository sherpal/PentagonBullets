package gamestate.actions

import custommath.Complex
import entities.LaserLauncher
import gamestate.{ActionSource, GameAction, GameState}

final case class NewLaserLauncher(
    actionId: Long,
    time: Long,
    laserLauncherId: Long,
    pos: Complex,
    ownerId: Long,
    actionSource: ActionSource
) extends GameAction {

  def setId(newId: Long): NewLaserLauncher = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def applyDefault(gameState: GameState): GameState = gameState.withLaserLauncher(
    laserLauncherId,
    time,
    new LaserLauncher(
      laserLauncherId,
      pos.re,
      pos.im,
      ownerId
    )
  )

}
