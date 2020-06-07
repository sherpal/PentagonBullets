package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}
import physics.Polygon

final case class GameBegins(
    actionId: Long,
    time: Long,
    gameBounds: Polygon,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.start(time, gameBounds)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
