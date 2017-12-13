package gamestate.actions

import custommath.Complex
import entities.Barrier
import gamestate.{ActionSource, GameAction, GameState}

final case class NewBarrier(
                             actionId: Long,
                             time: Long,
                             id: Long,
                             ownerId: Long,
                             teamId: Int,
                             pos: Complex,
                             rotation: Double,
                             actionSource: ActionSource
                           ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withBarrier(
    id, time, new Barrier(id, time, ownerId, teamId, pos.re, pos.im, rotation, Barrier.shape)
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
