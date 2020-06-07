package gamestate.actions

import entities.Bullet
import gamestate.{ActionSource, GameAction, GameState}

/**
  * Changes the radius of a given bullet.
  *
  * This may happen if a bullet hits a [[entities.BulletAmplifier]]
  */
final case class ChangeBulletRadius(
    actionId: Long,
    time: Long,
    bulletId: Long,
    newRadius: Int,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val bullet = gameState.bullets(bulletId)
    gameState.withBullet(
      bullet.id,
      time,
      new Bullet(
        bullet.id,
        bullet.time,
        bullet.ownerId,
        bullet.teamId,
        bullet.xPos,
        bullet.yPos,
        newRadius,
        bullet.direction,
        bullet.speed,
        bullet.travelledDistance
      )
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
