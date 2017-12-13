package gamestate.actions

import custommath.Complex
import entities.TeamFlag
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Adds a Flag to the state.
 * This can also be used to put the flag back to its original place, when a player drops it.
 */
final case class NewTeamFlag(
                              actionId: Long,
                              time: Long,
                              flagId: Long,
                              teamNbr: Int,
                              pos: Complex,
                              actionSource: ActionSource
                            ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withFlag(
    time, new TeamFlag(flagId, pos.re, pos.im, teamNbr, None, Nil)
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
