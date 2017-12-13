package gamestate.actions

import custommath.Complex
import entities.Obstacle
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Add a new [[Obstacle]] to the game.
 */
final case class NewObstacle(
                              actionId: Long,
                              time: Long,
                              id: Long,
                              pos: Complex,
                              vertices: Vector[Complex],
                              actionSource: ActionSource
                            ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withObstacle(
    id, time, Obstacle(id, pos, vertices)
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
