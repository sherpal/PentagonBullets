package gamestate.actions

import abilities.Ability
import entities.Player
import gamestate.{ActionSource, GameAction, GameState}

final case class UseAbilityAction(
    actionId: Long,
    time: Long,
    ability: Ability,
    useId: Long,
    actionSource: ActionSource
) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    val player = gameState.players(ability.casterId)
    gameState.withPlayer(
      player.id,
      time,
      new Player(
        player.id,
        player.team,
        time,
        player.name,
        player.xPos,
        player.yPos,
        player.direction,
        player.speed,
        player.moving,
        player.rotation,
        player.shape,
        player.lifeTotal,
        player.allowedAbilities,
        player.relevantUsedAbilities + (useId -> ability)
      )
    )
  }

  override def canHappen(gameState: GameState): Boolean = gameState.isPlayerAlive(ability.casterId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
