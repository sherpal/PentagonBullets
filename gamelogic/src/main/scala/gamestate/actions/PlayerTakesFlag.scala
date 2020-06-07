package gamestate.actions

import entities.TeamFlag
import gamestate.{ActionSource, GameAction, GameState}

final case class PlayerTakesFlag(
    actionId: Long,
    time: Long,
    flagId: Long,
    playerId: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val flag = gameState.flags.values.find(_.id == flagId).get
    gameState.withFlag(
      time,
      new TeamFlag(flagId, flag.xPos, flag.yPos, flag.teamNbr, Some(playerId), flag.takenBy)
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
