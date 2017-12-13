package gamestate.actions

import custommath.Complex
import entities.GunTurret
import gamestate.{ActionSource, GameAction, GameState}

final case class NewGunTurret(
                               actionId: Long,
                               time: Long,
                               turretId: Long,
                               ownerId: Long,
                               teamId: Int,
                               pos: Complex,
                               radius: Double,
                               actionSource: ActionSource
                             ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withGunTurret(
    turretId, time, new GunTurret(
      time, turretId, ownerId, teamId, pos.re, pos.im, time, radius, 0, GunTurret.maxLifeTotal
    )
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
