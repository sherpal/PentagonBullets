package gamestate.actions

import gamestate.{ActionSource, GameAction, GameState}

/**
 * Happens when a zone lost all its supply.
 */
final case class DestroyHealingZone(
                                     actionId: Long,
                                     time: Long,
                                     zoneId: Long,
                                     actionSource: ActionSource
                                   ) extends GameAction{

  def applyDefault(gameState: GameState): GameState = gameState.removeHealingZone(zoneId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
