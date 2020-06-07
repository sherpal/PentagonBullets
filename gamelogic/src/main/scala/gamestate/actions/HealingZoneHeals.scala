package gamestate.actions

import entities.{HealingZone, Player}
import gamestate.{ActionSource, GameAction, GameState}

/**
  * Happens when a [[entities.HealingZone]] heals someone.
  */
final case class HealingZoneHeals(
    actionId: Long,
    time: Long,
    healedUnitId: Long,
    healingZoneId: Long,
    amount: Double,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val player = gameState.players(healedUnitId)
    val zone   = gameState.healingZones(healingZoneId)
    if (player.lifeTotal == Player.maxLife) {
      gameState
    } else {
      gameState
        .withPlayer(
          healedUnitId,
          time,
          new Player(
            healedUnitId,
            player.team,
            player.time,
            player.name,
            player.xPos,
            player.yPos,
            player.direction,
            player.speed,
            player.moving,
            player.rotation,
            player.shape,
            math.min(Player.maxLife, player.lifeTotal + amount),
            player.allowedAbilities,
            player.relevantUsedAbilities
          )
        )
        .withHealingZone(
          healingZoneId,
          time,
          new HealingZone(
            zone.id,
            zone.creationTime,
            zone.ownerId,
            zone.lastTick,
            zone.lifeSupply - amount,
            zone.xPos,
            zone.yPos,
            zone.shape
          )
        )
    }
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
