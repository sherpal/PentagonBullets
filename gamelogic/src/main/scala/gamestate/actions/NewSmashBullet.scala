package gamestate.actions

import custommath.Complex
import entities.SmashBullet
import gamestate.{ActionSource, GameAction, GameState}

final case class NewSmashBullet(
    actionId: Long,
    time: Long,
    bulletId: Long,
    ownerId: Long,
    pos: Complex,
    dir: Double,
    radius: Int,
    speed: Double,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withSmashBullet(
    bulletId,
    time,
    new SmashBullet(
      bulletId,
      time,
      ownerId,
      pos.re,
      pos.im,
      radius,
      dir,
      speed,
      0,
      time
    )
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
