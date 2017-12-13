package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Happens when a Player takes damage from a specified source, other than a Bullet.
 */
final case class PlayerTakeDamage(
                                   actionId: Long,
                                   time: Long,
                                   playerId: Long,
                                   sourceId: Long,
                                   damage: Double,
                                   actionSource: ActionSource
                                 ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val player = gameState.players(playerId)
    gameState.withPlayer(
      playerId, time, new Player(
        playerId, player.team, player.time, player.name, player.xPos, player.yPos,
        player.direction, player.speed, player.moving, player.rotation,
        player.shape, player.lifeTotal - damage,
        player.allowedAbilities, player.relevantUsedAbilities
      )
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
