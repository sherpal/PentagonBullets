package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}


final case class ActionChangerEnded(
                                     actionId: Long,
                                     time: Long,
                                     actionChangerId: Long,
                                     actionSource: ActionSource
                                   ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.actionChangers(actionChangerId).end(
    gameState.removeActionChanger(actionChangerId, time)
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
