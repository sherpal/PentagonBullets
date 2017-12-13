package gamestate.actions

import entities.GunTurret
import gamestate.{ActionSource, GameAction, GameState}

final case class GunTurretTakesDamage(
                                       actionId: Long,
                                       time: Long,
                                       turretId: Long,
                                       damage: Double,
                                       actionSource: ActionSource
                                     ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val turret = gameState.gunTurrets(turretId)

    gameState.withGunTurret(turretId, time, new GunTurret(
      turret.creationTime, turretId, turret.ownerId, turret.teamId, turret.xPos, turret.yPos, turret.lastShot,
      turret.radius, turret.rotation, turret.lifeTotal - damage
    ))
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
