package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}

final case class PlayerHitByMultipleBullets(
                                             actionId: Long,
                                             time: Long,
                                             bulletIds: List[Long],
                                             playerId: Long,
                                             totalDamage: Double,
                                             actionSource: ActionSource
                                           ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = bulletIds.foldLeft(gameState.withPlayer(
    playerId, time, Player.bulletHitPlayer(gameState.players(playerId), totalDamage, time)
  ))((state: GameState, bulletId: Long) => state.removeBullet(bulletId, time))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}


