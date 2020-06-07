package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}

/**
  * GameAction that happen when a new Player comes in onto the game, or if a Player property changes.
  */
final case class NewPlayer(
    actionId: Long,
    player: Player,
    time: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState =
    gameState.withPlayer(player.id, time, player).removeDeadPlayer(player.id, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
