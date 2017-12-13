package gamestate.actions

import entities.ActionChanger
import gamestate.{ActionSource, GameAction, GameState}


final case class NewActionChanger(
                                   actionId: Long,
                                   time: Long,
                                   actionChanger: ActionChanger,
                                   actionSource: ActionSource
                                 ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = actionChanger.start(gameState.withActionChanger(
    actionChanger.id, time, actionChanger
  ))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
