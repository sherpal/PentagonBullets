package entities

import gamestate.{GameAction, GameState}
import gamestate.actions.{PlayerHitByBullet, PlayerHitByMultipleBullets, PlayerHitBySmashBullet, PlayerTakeDamage}

/**
  * While a Shield is active on a Player, he or she does not take any damage.
  */
class Shield(val id: Long, val time: Long, val playerId: Long) extends PlayerBuff {

  val duration: Long = 5000

  def changeAction(action: GameAction): List[GameAction] =
    List({
      action match {
        case PlayerTakeDamage(actionId, currentTime, plrId, srcId, _, source)
            if currentTime - time <= duration && plrId == playerId =>
          PlayerTakeDamage(actionId, currentTime, plrId, srcId, 0, source)
        case PlayerHitByBullet(actionId, plrId, bulletId, _, currentTime, source)
            if currentTime - time <= duration && plrId == playerId =>
          PlayerHitByBullet(actionId, plrId, bulletId, 0, currentTime, source)
        case PlayerHitByMultipleBullets(actionId, currentTime, bulletIds, plrId, _, source)
            if currentTime - time <= duration &&
              playerId == plrId =>
          PlayerHitByMultipleBullets(actionId, currentTime, bulletIds, plrId, 0, source)
        case PlayerHitBySmashBullet(actionId, currentTime, plrId, srcId, source)
            if currentTime - time <= duration && plrId == playerId =>
          PlayerTakeDamage(actionId, currentTime, plrId, srcId, 0, source)
        case _ =>
          action
      }
    })

  def start(gameState: GameState): GameState = gameState

  def end(gameState: GameState): GameState = gameState

}
