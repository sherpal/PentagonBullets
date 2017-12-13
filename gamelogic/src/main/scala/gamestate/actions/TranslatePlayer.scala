package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}


final case class TranslatePlayer(
                                  actionId: Long,
                                  time: Long,
                                  playerId: Long,
                                  x: Double, y: Double,
                                  actionSource: ActionSource
                                ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val player = gameState.players(playerId)
    gameState.withPlayer(
      playerId, time, new Player(
        playerId, player.team, time, player.name, x, y,
        player.direction, player.speed, player.moving, player.rotation, lifeTotal = player.lifeTotal,
        allowedAbilities = player.allowedAbilities, relevantUsedAbilities = player.relevantUsedAbilities
      )
    )
  }

  override def canHappen(gameState: GameState): Boolean = gameState.isPlayerAlive(playerId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
