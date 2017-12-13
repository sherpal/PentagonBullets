package gamestate.actions

import entities.AbilityGiver
import gamestate.{ActionSource, GameAction, GameState}

/**
 * A Player takes the AbilityGiver, removing it from the game, and then receives the ability.
 */
final case class PlayerTakeAbilityGiver(
                                         actionId: Long,
                                         time: Long,
                                         playerId: Long,
                                         abilityGiverId: Long,
                                         abilityId: Int,
                                         actionSource: ActionSource
                                       ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = gameState
    .removeAbilityGiver(abilityGiverId, time)
    .withPlayer(playerId, time, AbilityGiver.playerTakeAbilityGiver(
      gameState.players(playerId), time, gameState.abilityGivers(abilityGiverId).abilityId)
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
