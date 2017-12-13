package gamestate.actions

import entities.TeamFlag
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Happens when the flag bearer is killed. The Flag goes back to its original position.
 */
final case class PlayerDropsFlag(
                                  actionId: Long,
                                  time: Long,
                                  flagId: Long,
                                  actionSource: ActionSource
                                ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val flag = gameState.flags.values.find(_.id == flagId).get
    gameState.withFlag(
      time, new TeamFlag(flagId, flag.xPos, flag.yPos, flag.teamNbr, None, flag.takenBy)
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
