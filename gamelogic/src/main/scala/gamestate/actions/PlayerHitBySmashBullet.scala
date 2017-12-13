package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}


final case class PlayerHitBySmashBullet(
                                         actionId: Long,
                                         time: Long,
                                         playerId: Long,
                                         bulletId: Long,
                                         actionSource: ActionSource
                                       ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withPlayer(
    playerId, time, Player.smashBulletHitPlayer(
      gameState.players(playerId), time
    )
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
