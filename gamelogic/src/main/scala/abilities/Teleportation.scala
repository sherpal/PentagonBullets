package abilities

import custommath.Complex
import gamestate.ActionSource.AbilitySource
import gamestate.actions.TranslatePlayer
import gamestate.{GameAction, GameState}

/**
 * The Unit goes instantly to the endPos position.
 */
class Teleportation(val time: Long, val useId: Long, val casterId: Long, val startingPos: Complex,
                    val endPos: Complex) extends Ability {

  val id: Int = Ability.teleportationId

  val cooldown: Long = 20000

  def copyWithUseId(newUseId: Long, newTime: Long): Teleportation = new Teleportation(
    newTime, newUseId, casterId, startingPos, endPos
  )

  def isLegal(gameState: GameState): Boolean = gameState.isLivingUnitAlive(casterId) &&
   {
     // checking if entity does not collide anything after teleportation
     val entity = gameState.players(casterId)
     !gameState.collidingPlayerObstacles(entity)
       .exists(obs => obs.shape.collides(obs.pos, 0, entity.shape, endPos, entity.rotation)) &&
       gameState.gameBounds.collides(Complex(0, 0), 0, entity.shape, endPos, entity.rotation)
   }

  def createActions: List[GameAction] = {
    List(
      TranslatePlayer(GameAction.newId(), time, casterId, endPos.re, endPos.im, AbilitySource)
    )
  }

}
