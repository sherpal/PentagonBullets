package abilities

import custommath.Complex
import entities.{Barrier, Entity}
import gamestate.ActionSource.AbilitySource
import gamestate.actions.NewBarrier
import gamestate.{GameAction, GameState}

class CreateBarrier(val time: Long, val useId: Long, val casterId: Long, val teamId: Int,
                    val targetPos: Complex, val rotation: Double) extends Ability {


  val id: Int = Ability.createBarrierId

  val cooldown: Long = 20000

  def copyWithUseId(newUseId: Long, newTime: Long): CreateBarrier = new CreateBarrier(
    newTime, newUseId, casterId, teamId, targetPos, rotation
  )

  def isLegal(gameState: GameState): Boolean = {
    gameState.players.get(casterId) match {
      case Some(player) =>
        !gameState.players.values.filter(_.team != player.team).exists(
          p => p.shape.collides(p.pos, p.rotation, Barrier.shape, targetPos, rotation)
        )
        // can't put a barrier directly on a player
      case None =>
        false
    }
  }

  def createActions: List[GameAction] = List(
    NewBarrier(GameAction.newId(), time, Entity.newId(), casterId, teamId, targetPos, rotation, AbilitySource)
  )

}
