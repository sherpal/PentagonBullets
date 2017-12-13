package gamestate.actions

import entities.HealingZone
import gamestate.{ActionSource, GameAction, GameState}

/**
 * Happes when a healing zone has to be Updated.
 */
final case class UpdateHealingZone(
                                    actionId: Long,
                                    time: Long,
                                    zoneId: Long,
                                    ownerId: Long,
                                    lifeSupply: Double,
                                    xPos: Double, yPos: Double,
                                    actionSource: ActionSource
                                  ) extends GameAction {


  def applyDefault(gameState: GameState): GameState = gameState.withHealingZone(
    zoneId, time, HealingZone(
      zoneId, gameState.healingZones(zoneId).creationTime, ownerId, time, lifeSupply, xPos, yPos
    )
  )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
