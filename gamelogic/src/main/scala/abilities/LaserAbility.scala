package abilities

import custommath.Complex
import entities.Entity
import gamestate.ActionSource.AbilitySource
import gamestate.actions._
import gamestate.{GameAction, GameState}

class LaserAbility(
                    val time: Long, val useId: Long, val casterId: Long,
                    val teamId: Int, val stepNumber: Int, val pos: Complex
                  ) extends MultiStepAbility {

  val cooldown: Long = 8000

  val id: Int = Ability.laserId

  val innerCooldown: Vector[Long] = Vector(2000, cooldown)

  def createActions(gameState: GameState): List[GameAction] = if (stepNumber == 0) {
    List(NewLaserLauncher(GameAction.newId(), time, Entity.newId(), pos, casterId, AbilitySource))
  } else {
    (gameState.players.get(casterId), gameState.laserLaunchers.values.find(_.ownerId == casterId)) match {
      case (Some(caster), Some(laserLauncher)) =>
        val casterPos: Complex = caster.currentPosition(time - caster.time)
        DestroyLaserLauncher(GameAction.newId(), time, laserLauncher.id, AbilitySource) +:
          FireLaser(GameAction.newId(), time, casterId, casterPos, laserLauncher.pos, AbilitySource) +:
          (gameState.gunTurrets.values
          .filterNot(_.teamId == teamId)
          .filter(turret =>
            turret.shape.intersectSegment(
              turret.pos, 0, laserLauncher.pos, casterPos
            )
          )
          .map(
            turret => GunTurretTakesDamage(GameAction.newId(), time, turret.id, LaserAbility.damage, AbilitySource)
          ) ++
          gameState.players.values
          .filterNot(_.team == teamId)
          .filter(player =>
            player.shape.intersectSegment(
              player.currentPosition(time - player.time), player.rotation, laserLauncher.pos, casterPos
            ))
          .map(player => PlayerTakeDamage(
            GameAction.newId(), time, player.id, casterId, LaserAbility.damage, AbilitySource)
          ))
          .toList
      case _ =>
        Nil
    }

  }

  def isLegal(gameState: GameState): Boolean = gameState.isPlayerAlive(casterId)

  def copyWithUseId(newUseId: Long, newTime: Long): Ability = new LaserAbility(
    newTime, newUseId, casterId, teamId, stepNumber, pos
  )

}

object LaserAbility {

  val damage: Double = 30

}