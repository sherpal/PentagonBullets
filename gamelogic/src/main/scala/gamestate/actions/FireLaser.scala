package gamestate.actions

import custommath.Complex
import gamestate.{ActionSource, GameAction, GameState}

final case class FireLaser(
                            actionId: Long,
                            time: Long,
                            ownerId: Long,
                            pos1: Complex,
                            pos2: Complex,
                            actionSource: ActionSource
                          ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState

  def setId(newId: Long): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

}
