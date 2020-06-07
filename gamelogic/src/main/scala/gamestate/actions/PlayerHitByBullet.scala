package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}

/**
  * PlayerHitByBullet when a Player is hit by an opponent's bullet.
  */
final case class PlayerHitByBullet(
    actionId: Long,
    playerId: Long,
    bulletId: Long,
    damage: Double,
    time: Long,
    actionSource: ActionSource
) extends GameAction {

  // the bullet that hit is removed
  // the player will lose life
  def applyDefault(gameState: GameState): GameState =
    gameState
      .removeBullet(bulletId, time)
      .withPlayer(
        playerId,
        time,
        Player.bulletHitPlayer(
          gameState.players(playerId),
          damage,
          time
        )
      )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
