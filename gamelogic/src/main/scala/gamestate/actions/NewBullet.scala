package gamestate.actions

import custommath.Complex
import entities.Bullet
import gamestate.{ActionSource, GameAction, GameState}

/**
 * GameAction when a player shoots a Bullet
 *
 * @param playerId ID of the player that shoots the player
 * @param pos      starting position of the Bullet
 * @param dir      direction in which the Bullet goes
 * @param speed    the speed of the Bullet. Usually Bullet.speed.
 * @param time     time at which the Bullet was fired
 * @param travelledDistance the distance already travelled by the bullet.
 */
final case class NewBullet(
                            actionId: Long,
                            id: Long,
                            playerId: Long,
                            teamId: Int,
                            pos: Complex,
                            radius: Int,
                            dir: Double,
                            speed: Int,
                            time: Long,
                            travelledDistance: Double,
                            actionSource: ActionSource
                          ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withBullet(id, time, new Bullet(
    id, time, playerId, teamId, pos.re, pos.im,
    radius, dir, speed, travelledDistance
  ))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
