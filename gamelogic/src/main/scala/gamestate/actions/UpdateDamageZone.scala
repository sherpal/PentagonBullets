package gamestate.actions

import entities.DamageZone
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Updates (or creates) the DamageZone at the specified position and with the specified radius.
 */
final case class UpdateDamageZone(
                                   actionId: Long,
                                   time: Long,
                                   id: Long,
                                   lastGrow: Long,
                                   lastTick: Long,
                                   xPos: Double, yPos: Double,
                                   radius: Int,
                                   actionSource: ActionSource
                                 ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withDamageZone(
    id, time, DamageZone(id, lastGrow, lastTick, xPos, yPos, radius)
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
