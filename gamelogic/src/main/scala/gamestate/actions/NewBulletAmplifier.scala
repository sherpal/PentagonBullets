package gamestate.actions

import custommath.Complex
import entities.BulletAmplifier
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Adds a BulletAmplifier to the game.
 */
final case class NewBulletAmplifier(
                                     actionId: Long,
                                     time: Long,
                                     id: Long,
                                     ownerId: Long,
                                     rotation: Double,
                                     pos: Complex,
                                     actionSource: ActionSource
                                   ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState.withBulletAmplifier(
    id, time, new BulletAmplifier(
      id, time, ownerId, pos.re, pos.im, BulletAmplifier.bulletAmplifierShape, rotation, Nil
    )
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
