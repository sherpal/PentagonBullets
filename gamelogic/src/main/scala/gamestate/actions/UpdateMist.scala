package gamestate.actions

import entities.Mist
import gamestate.{ActionSource, GameAction, GameState}

/**
  * Updates an existing Mist or create an other one.
  */
final case class UpdateMist(
    actionId: Long,
    time: Long,
    id: Long,
    lastGrow: Long,
    lastTick: Long,
    sideLength: Double,
    gameAreaSideLength: Int,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState =
    // Cutting into two cases will boosts performance
    gameState.mists.get(id) match {
      case Some(mist) if mist.lastTick != lastTick =>
        // We are updating the lastTick, no need to recreate a shape
        gameState.withMist(id, time, new Mist(id, lastGrow, lastTick, mist.shape))
      case _ =>
        // Either it's a new Mist, or we update the size. Either way, we need a new shape.
        gameState.withMist(
          id,
          time,
          new Mist(id, lastGrow, lastTick, Mist.makeMistShape(sideLength, gameAreaSideLength))
        )
    }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
