package gamestate.actions

import entities.HealUnit
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Happens when a player goes onto a HealUnit
 */
final case class PlayerTakeHealUnit(
                                     actionId: Long,
                                     time: Long,
                                     playerId: Long,
                                     healUnitId: Long,
                                     actionSource: ActionSource
                                   ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState
    .removeHealUnit(healUnitId, time)
    .withPlayer(playerId, time, HealUnit.playerTakeUnit(gameState.players(playerId), time))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
