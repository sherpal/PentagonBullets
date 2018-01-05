package abilities

import custommath.Complex
import entities.{Entity, Player}
import gamestate.ActionSource.AbilitySource
import gamestate.actions._
import gamestate.{GameAction, GameState}
import physics.Polygon

class LaserAbility(
                    val time: Long, val useId: Long, val casterId: Long,
                    val teamId: Int, val stepNumber: Int, val pos: Complex
                  ) extends MultiStepAbility {

  val cooldown: Long = 8000

  val id: Int = Ability.laserId

  val innerCooldown: Vector[Long] = Vector(1000, cooldown)

  def createActions(gameState: GameState): List[GameAction] = if (stepNumber == 0) {
    List(NewLaserLauncher(GameAction.newId(), time, Entity.newId(), pos, casterId, AbilitySource))
  } else {
    (gameState.players.get(casterId), gameState.laserLaunchers.values.find(_.ownerId == casterId)) match {
      case (Some(caster), Some(laserLauncher)) =>
        val casterPos: Complex = caster.currentPosition(time - caster.time)

        val directionPos = laserLauncher.pos - casterPos
        val unitVec = directionPos / directionPos.modulus
        val perpendicularVec = Player.radius * Complex(-unitVec.im, unitVec.re)

        val laserVertices =
          Vector(casterPos, laserLauncher.pos - perpendicularVec, laserLauncher.pos + perpendicularVec)

        val laserShape = Polygon(laserVertices, convex = true)

        DestroyLaserLauncher(GameAction.newId(), time, laserLauncher.id, AbilitySource) +:
          FireLaser(GameAction.newId(), time, casterId, laserVertices, AbilitySource) +:
          (gameState.gunTurrets.values
          .filterNot(_.teamId == teamId)
          .filter(turret =>
//            turret.shape.intersectSegment(
//              turret.pos, 0, laserLauncher.pos, casterPos
//            )
            turret.shape.collides(turret.pos, 0, laserShape, 0, 0)
          )
          .map(
            turret => GunTurretTakesDamage(GameAction.newId(), time, turret.id, LaserAbility.damage, AbilitySource)
          ) ++
          gameState.players.values
          .filterNot(_.team == teamId)
          .filter(player =>
//            player.shape.intersectSegment(
//              player.currentPosition(time - player.time), player.rotation, laserLauncher.pos, casterPos
//            ))
            player.shape.collides(
              player.currentPosition(time - player.time), player.rotation, laserShape, 0, 0)
            )
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