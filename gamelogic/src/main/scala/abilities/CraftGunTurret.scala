package abilities

import custommath.Complex
import entities.{Entity, GunTurret}
import gamestate.ActionSource.AbilitySource
import gamestate.{GameAction, GameState}
import gamestate.actions.NewGunTurret

class CraftGunTurret(val time: Long, val useId: Long, val casterId: Long, val teamId: Int, val pos: Complex)
  extends Ability {

  def copyWithUseId(newUseId: Long, newTime: Long): CraftGunTurret =
    new CraftGunTurret(newTime, newUseId, casterId, teamId, pos)

  def createActions: List[GameAction] = List(
    NewGunTurret(
      GameAction.newId(), time, Entity.newId(), casterId, teamId, pos, GunTurret.defaultRadius, AbilitySource
    )
  )

  def isLegal(gameState: GameState): Boolean = gameState.isPlayerAlive(casterId)

  val cooldown: Long = 30000

  val id: Int = Ability.craftGunTurretId

}
