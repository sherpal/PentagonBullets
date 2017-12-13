package gamestate.actions

import entities.Player
import gamestate.{ActionSource, GameAction, GameState}


final case class RemoveRelevantAbility(
                                        actionId: Long,
                                        time: Long,
                                        entityId: Long,
                                        useId: Long,
                                        actionSource: ActionSource
                                      ) extends GameAction {

  def applyDefault(gameState: GameState): GameState = {
    gameState.withAbilities.get(entityId) match {
      case Some(entity) =>
        entity match {
          case entity: Player => gameState.withPlayer(entityId, time, new Player(
            entityId, entity.team, time, entity.name, entity.xPos, entity.yPos, entity.direction, entity.speed,
            entity.moving, entity.rotation, entity.shape, entity.lifeTotal,
            entity.allowedAbilities, entity.relevantUsedAbilities - useId
          ))
          case _ => gameState // need to change if other entities can have abilities in the future
        }
      case _ =>
        gameState
    }
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: Long): GameAction = copy(actionId = newId)

}
