package gamestate.actions

import entities.TeamFlag
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Happens when a Player manages to bring an enemy flag back to his or her side, scoring one point.
 */
final case class PlayerBringsFlagBack(
                                       actionId: Long,
                                       time: Long,
                                       flagId: Long,
                                       playerId: Long,
                                       actionSource: ActionSource
                                     ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val flag = gameState.flags.values.find(_.id == flagId).get
    val takenByTeam = gameState.players(playerId).team
    gameState.withFlag(
      time, new TeamFlag(flagId, flag.xPos, flag.yPos, flag.teamNbr, None, takenByTeam +: flag.takenBy)
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
