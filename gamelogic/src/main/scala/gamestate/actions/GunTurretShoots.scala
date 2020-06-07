package gamestate.actions

import entities.{Bullet, GunTurret}
import gamestate.{ActionSource, GameAction, GameState}

final case class GunTurretShoots(
    actionId: Long,
    time: Long,
    turretId: Long,
    rotation: Double,
    bulletId: Long,
    bulletRadius: Int,
    bulletSpeed: Double,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val turret = gameState.gunTurrets(turretId)
    gameState
      .withBullet(
        bulletId,
        time,
        new Bullet(
          bulletId,
          time,
          turret.ownerId,
          turret.teamId,
          turret.xPos,
          turret.yPos,
          bulletRadius,
          rotation,
          bulletSpeed,
          0
        )
      )
      .withGunTurret(
        turretId,
        time,
        new GunTurret(
          turret.creationTime,
          turretId,
          turret.ownerId,
          turret.teamId,
          turret.xPos,
          turret.yPos,
          time,
          turret.radius,
          rotation,
          turret.lifeTotal
        )
      )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
