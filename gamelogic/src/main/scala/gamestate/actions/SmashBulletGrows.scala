package gamestate.actions

import entities.SmashBullet
import gamestate.{ActionSource, GameAction, GameState}

final case class SmashBulletGrows(
                                   actionId: Long,
                                   time: Long,
                                 smashBulletId: Long,
                                 newRadius: Int,
                                 actionSource: ActionSource
                                 ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val bullet = gameState.smashBullets(smashBulletId)
    gameState.withSmashBullet(
      smashBulletId, time, new SmashBullet(
        smashBulletId, bullet.time, bullet.ownerId, bullet.xPos, bullet.yPos, newRadius,
        bullet.direction, bullet.speed, bullet.travelledDistance, time
      )
    )
  }

  def changeTime(newTime: Long): SmashBulletGrows = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
