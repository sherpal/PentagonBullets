package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}

/**
  * Sent by the players when they move around.
  */
final case class UpdatePlayerPos(
    actionId: Long,
    time: Long,
    playerId: Long,
    x: Double,
    y: Double,
    dir: Double,
    moving: Boolean,
    rot: Double,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val player = gameState.players(playerId)
    gameState.withPlayer(
      playerId,
      time,
      new Player(
        playerId,
        player.team,
        time,
        player.name,
        x,
        y,
        direction             = dir,
        moving                = moving,
        rotation              = rot,
        lifeTotal             = player.lifeTotal,
        allowedAbilities      = player.allowedAbilities,
        relevantUsedAbilities = player.relevantUsedAbilities
      )
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
