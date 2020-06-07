package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

/**
  * PlayerDead happens when a Player dies.
  */
final case class PlayerDead(
    actionId: Long,
    time: Long,
    playerId: Long,
    playerName: String,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState =
    gameState.removePlayer(playerId, time).withDeadPlayer(playerId, time, gameState.players(playerId))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
