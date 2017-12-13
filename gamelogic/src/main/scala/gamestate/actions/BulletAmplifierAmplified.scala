package gamestate.actions

import entities.BulletAmplifier
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Adds a Bullet id to the list of amplified bullets.
 */
final case class BulletAmplifierAmplified(
                                           actionId: Long,
                                           time: Long,
                                           bulletId: Long,
                                           amplifierId: Long,
                                           actionSource: ActionSource
                                         ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val amplifier = gameState.bulletAmplifiers(amplifierId)
    gameState.withBulletAmplifier(
      amplifier.id, time, new BulletAmplifier(
        amplifier.id, amplifier.creationTime, amplifier.ownerId, amplifier.xPos, amplifier.yPos,
        amplifier.shape, amplifier.rotation, amplifier.addBulletAmplified(bulletId)
      )
    )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
