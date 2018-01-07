package gamemessages

import abilities._
import custommath.Complex
import entities.{BulletGlue, Player, Shield}
import exceptions.{ActionChangerMissing, ForgotToAddCaseMatchException}
import gamestate._
import gamestate.actions.{NewPlayer => NewPlayerAction, _}
import networkcom.Message
import networkcom.messages._
import physics.Polygon

import scala.language.implicitConversions



/**
 * This Object will transform [[GameAction]]s into [[Message]]s in order to send them.
 */
object MessageMaker {

  private implicit def complexToPoint(z: Complex): Point = Point(z.re, z.im)

  private implicit def complexVectorToPointVector(zs: Vector[Complex]): Vector[Point] = zs.map(complexToPoint)

  private implicit def actionSourceToString(actionSource: ActionSource): String = actionSource.toString

  private def newPlayer(gameName: String, newPlayerAction: NewPlayerAction): NewPlayerMessage = {
    val player = newPlayerAction.player
    NewPlayerMessage(
      newPlayerAction.actionId,
      gameName, newPlayerAction.actionSource,
      newPlayerAction.time, player.name, player.id, player.xPos, player.yPos, player.team,
      player.allowedAbilities
    )
  }

  private def playerTakeHealUnit(gameName: String, action: PlayerTakeHealUnit): PlayerTakeHealUnitMessage =
    PlayerTakeHealUnitMessage(
      action.actionId,
      gameName, action.actionSource,
      action.time, action.playerId, action.healUnitId
    )

  private def playerTakeDamage(gameName: String, action: PlayerTakeDamage): PlayerTakeDamageMessage =
    PlayerTakeDamageMessage(
      action.actionId,
      gameName, action.actionSource,
      action.time, action.playerId, action.sourceId, action.damage
    )

  private def updateDamageZone(gameName: String, action: UpdateDamageZone): UpdateDamageZoneMessage =
    UpdateDamageZoneMessage(
      action.actionId,
      gameName, action.actionSource,
      action.time, action.id, action.lastGrow, action.lastTick,
      action.xPos, action.yPos, action.radius
    )

  def toMessage(gameName: String, action: GameAction): ActionMessage = action match {
    case UpdatePlayerPos(actionId, time, playerId, x, y, dir, moving, rot, actionSource) =>
      UpdatePlayerPosMessage(actionId, gameName, actionSource, time, playerId, x, y, dir, moving, rot)

    case NewBullet(actionId, bulletId, playerId, teamId, pos, radius, dir, speed, time, travelledDistance, actionSource) =>
      NewBulletMessage(
        actionId, gameName, actionSource,
        time, bulletId, playerId, teamId, pos.re, pos.im, radius, dir, speed, travelledDistance
      )

    case DestroyBullet(actionId, bulletId, time, actionSource) =>
      DestroyBulletMessage(actionId, gameName, actionSource, time, bulletId)

    case PlayerHitByMultipleBullets(actionId, time, bulletIds, playerId, totalDamage, actionSource) =>
      PlayerHitByBulletsMessage(actionId, gameName, actionSource, time, bulletIds, playerId, totalDamage)

    case PlayerHitByBullet(actionId, playerId, bulletId, damage, time, actionSource) =>
      PlayerHitMessage(actionId, gameName, actionSource, time, playerId, bulletId, damage)

    case NewHealUnit(actionId, time, id, pos, actionSource) =>
      NewHealUnitMessage(actionId, gameName, actionSource, time, id, pos.re, pos.im)

    case DestroyHealUnit(actionId, time, id, actionSource) =>
      DestroyHealUnitMessage(actionId, gameName, actionSource, time, id)

    case action: PlayerTakeHealUnit => playerTakeHealUnit(gameName, action)
    case action: PlayerTakeDamage => playerTakeDamage(gameName, action)
    case action: UpdateDamageZone => updateDamageZone(gameName, action)

    case UpdateMist(actionId, time, id, lastGrow, lastTick, sideLength, gameAreaSideLength, actionSource) =>
      UpdateMistMessage(actionId, gameName, actionSource, time, id, lastGrow, lastTick, sideLength, gameAreaSideLength)

    case DestroyDamageZone(actionId, time, zoneId, actionSource) =>
      DestroyDamageZoneMessage(actionId, gameName, actionSource, time, zoneId)

    case GunTurretShoots(actionId, time, turretId, rotation, bulletId, bulletRadius, bulletSpeed, actionSource) =>
      GunTurretShootsMessage(
        actionId, gameName, actionSource, time, turretId, rotation, bulletId, bulletRadius, bulletSpeed
      )

    case GunTurretTakesDamage(actionId, time, turretId, damage, actionSource) =>
      GunTurretTakesDamageMessage(actionId, gameName, actionSource, time, turretId, damage)

    case DestroyGunTurret(actionId, time, turretId, actionSource) =>
      DestroyGunTurretMessage(actionId, gameName, actionSource, time, turretId)

    case NewGunTurret(actionId, time, turretId, ownerId, teamId, pos, radius, actionSource) =>
      NewGunTurretMessage(actionId, gameName, actionSource, time, turretId, ownerId, teamId, pos, radius)

    case ActionChangerEnded(actionId, time, changerId, actionSource) =>
      ActionChangerEndedMessage(actionId, gameName, actionSource, time, changerId)

    case NewActionChanger(actionId, time, changer, actionSource) =>
      changer match {
        case changer: Shield =>
          NewShieldMessage(actionId, gameName, actionSource, time, changer.id, changer.playerId)
        case changer: BulletGlue =>
          NewBulletGlueMessage(actionId, gameName, actionSource, time, changer.id, changer.playerId, changer.teamId)
        case _ =>
          println(s"Changer ${changer.getClass} is not set in match of NewActionChanger in toMessage.")
          throw new ActionChangerMissing(changer.getClass.toString)
      }

    case UseAbilityAction(actionId, time, ability, useId, actionSource) =>
      ability match {
        case ability: ActivateShield =>
          ActivateShieldMessage(actionId, gameName, actionSource, time, useId, ability.playerId)
        case ability: BigBullet =>
          BigBulletMessage(
            actionId, gameName, actionSource,
            time, useId, ability.casterId, ability.teamId,
            ability.startingPos.re, ability.startingPos.im, ability.rotation
          )
        case ability: TripleBullet =>
          TripleBulletMessage(
            actionId, gameName, actionSource,
            time, useId, ability.casterId, ability.teamId,
            ability.startingPos.re, ability.startingPos.im, ability.rotation
          )
        case ability: Teleportation =>
          TeleportationMessage(
            actionId, gameName, actionSource,
            time, useId, ability.casterId, ability.startingPos, ability.endPos
          )
        case ability: CreateHealingZone =>
          CreateHealingZoneMessage(
            actionId, gameName, actionSource,
            time, useId, ability.casterId, ability.targetPos
          )
        case ability: CreateBulletAmplifier =>
          CreateBulletAmplifierMessage(
            actionId, gameName, actionSource, time, useId, ability.casterId, ability.targetPos, ability.rotation
          )
        case ability: LaunchSmashBullet =>
          LaunchSmashBulletMessage(
            actionId, gameName, actionSource, time, useId, ability.casterId, ability.startingPos, ability.rotation
          )
        case ability: CraftGunTurret =>
          CraftGunTurretMessage(
            actionId, gameName, actionSource, time, useId, ability.casterId, ability.teamId, ability.pos
          )
        case ability: CreateBarrier =>
          CreateBarrierMessage(
            actionId, gameName, actionSource, time, useId,
            ability.casterId, ability.teamId, ability.targetPos, ability.rotation
          )
        case ability: PutBulletGlue =>
          PutBulletGlueMessage(
            actionId, gameName, actionSource, time, useId, ability.casterId, ability.teamId
          )
        case ability: LaserAbility =>
          LaserAbilityMessage(
            actionId, gameName, actionSource, time, useId, ability.casterId, ability.teamId, ability.stepNumber,
            ability.pos
          )
        case _ =>
          println("You forgot to add an ability match in toMessage function.")
          throw new ForgotToAddCaseMatchException
      }

    case TranslatePlayer(actionId, time, playerId, x, y, actionSource) =>
      TranslatePlayerMessage(actionId, gameName, actionSource, time, playerId, x, y)

    case ChangeBulletRadius(actionId, time, bulletId, newRadius, actionSource) =>
      ChangeBulletRadiusMessage(actionId, gameName, actionSource, time, bulletId, newRadius)

    case UpdateHealingZone(actionId, time, zoneId, ownerId, lifeSupply, xPos, yPos, actionSource) =>
      UpdateHealingZoneMessage(actionId, gameName, actionSource, time, zoneId, ownerId, lifeSupply, xPos, yPos)

    case HealingZoneHeals(actionId, time, healedUnitId, healingZoneId, amount, actionSource) =>
      HealingZoneHealMessage(actionId, gameName, actionSource, time, healedUnitId, healingZoneId, amount)

    case DestroyHealingZone(actionId, time, zoneId, actionSource) =>
      DestroyHealingZoneMessage(actionId, gameName, actionSource, time, zoneId)

    case NewSmashBullet(actionId, time, bulletId, ownerId, pos, dir, radius, speed, actionSource) =>
      NewSmashBulletMessage(actionId, gameName, actionSource, time, bulletId, ownerId, pos, dir, radius, speed)

    case PlayerHitBySmashBullet(actionId, time, playerId, bulletId, actionSource) =>
      PlayerHitSmashBulletMessage(actionId, gameName, actionSource, time, playerId, bulletId)

    case SmashBulletGrows(actionId, time, smashBulletId, newRadius, actionSource) =>
      SmashBulletGrowsMessage(actionId, gameName, actionSource, time, smashBulletId, newRadius)

    case DestroySmashBullet(actionId, time, bulletId, actionSource) =>
      DestroySmashBulletMessage(actionId, gameName, actionSource, time, bulletId)

    case BulletAmplifierAmplified(actionId, time, bulletId, amplifierId, actionSource) =>
      BulletAmplifierAmplifiedMessage(actionId, gameName, actionSource, time, bulletId, amplifierId)

    case NewBulletAmplifier(actionId, time, id, ownerId, rotation, pos, actionSource) =>
      NewBulletAmplifierMessage(actionId, gameName, actionSource, time, id, ownerId, rotation, pos)

    case DestroyBulletAmplifier(actionId, time, id, actionSource) =>
      DestroyBulletAmplifierMessage(actionId, gameName, actionSource, time, id)



    case NewBarrier(actionId, time, id, ownerId, teamId, pos, rotation, actionSource) =>
      NewBarrierMessage(actionId, gameName, actionSource, time, id, ownerId, teamId, rotation, pos)

    case DestroyBarrier(actionId, time, id, actionSource) =>
      DestroyBarrierMessage(actionId, gameName, actionSource, time, id)

    case NewHealingZone(actionId, time, zoneId, ownerId, lifeSupply, xPos, yPos, actionSource) =>
      NewHealingZoneMessage(actionId, gameName, actionSource, time, zoneId, ownerId, lifeSupply, xPos, yPos)

    case NewLaserLauncher(actionId, time, laserLauncherId, pos, ownerId, actionSource) =>
      NewLaserLauncherMessage(actionId, gameName, actionSource, time, laserLauncherId, pos, ownerId)

    case DestroyLaserLauncher(actionId, time, laserLauncherId, actionSource) =>
      DestroyLaserLauncherMessage(actionId, gameName, actionSource, time, laserLauncherId)

    case FireLaser(actionId, time, ownerId, laserVertices, actionSource) =>
      FireLaserMessage(actionId, gameName, actionSource, time, ownerId, laserVertices)

    case RemoveRelevantAbility(actionId, time, entityId, useId, actionSource) =>
      RemoveRelevantAbilityMessage(actionId, gameName, actionSource, time, entityId, useId)

    case NewAbilityGiver(actionId, time, abilityGiverId, pos, abilityId, actionSource) =>
      NewAbilityGiverMessage(actionId, gameName, actionSource, time, abilityGiverId, pos, abilityId)

    case PlayerTakeAbilityGiver(actionId, time, playerId, abilityGiverId, abilityId, actionSource) =>
      PlayerTakeAbilityGiverMessage(actionId, gameName, actionSource, time, playerId, abilityGiverId, abilityId)

    case PlayerTakesFlag(actionId, time, flagId, playerId, actionSource) =>
      PlayerTakesFlagMessage(actionId, gameName, actionSource, time, flagId, playerId)

    case PlayerDropsFlag(actionId, time, flagId, actionSource) =>
      PlayerDropsFlagMessage(actionId, gameName, actionSource, time, flagId)

    case PlayerBringsFlagBack(actionId, time, flagId, playerId, actionSource) =>
      PlayerBringsFlagBackMessage(actionId, gameName, actionSource, time, flagId, playerId)

    case NewTeamFlag(actionId, time, flagId, teamNbr, pos, actionSource) =>
      NewTeamFlagMessage(actionId, gameName, actionSource, time, flagId, teamNbr, pos)

    case NewObstacle(actionId, time, id, pos, vertices, actionSource) =>
      NewObstacleMessage(actionId, gameName, actionSource, time, id, pos.re, pos.im, vertices)

    case action: PlayerDead =>
      PlayerDeadMessage(action.actionId, gameName, action.actionSource, action.time, action.playerId, action.playerName)

    case action: NewPlayerAction =>
      newPlayer(gameName, action)

    case GameBegins(actionId, time, gameBounds, actionSource) => GameBeginsMessage(
      actionId, gameName, actionSource, time, gameBounds.vertices
    )

    case action: GameEndedAction => GameEndedMessage(action.actionId, gameName, action.actionSource, action.time)

    case _ =>
      println(action.getClass.toString)
      throw new NotImplementedError(s"this action is not yet implemented ${action.getClass}")
  }


  def actionsMessage(gameName: String, actions: List[GameAction]): ActionsMessage = ActionsMessage(
    gameName, actions.map(toMessage(gameName, _))
  )


  def messageToAction(message: ActionMessage): GameAction = message match {
    case UpdatePlayerPosMessage(actionId, _, actionSource, time, id, x, y, dir, moving, rot) =>
      UpdatePlayerPos(actionId, time, id, x, y, dir, moving, rot, actionSource)

    case NewBulletMessage(actionId, _, actionSource, time, id,
    plrId, teamId, x, y, radius, dir, speed, travelledDistance) =>
      NewBullet(actionId, id, plrId, teamId, Complex(x, y), radius, dir, speed, time, travelledDistance, actionSource)

    case DestroyBulletMessage(actionId, _, actionSource, time, id) =>
      DestroyBullet(actionId, id, time, actionSource)

    case PlayerHitByBulletsMessage(actionId, _, actionSource, time, bulletIds, playerId, totalDamage) =>
      PlayerHitByMultipleBullets(actionId, time, bulletIds, playerId, totalDamage, actionSource)

    case PlayerHitMessage(actionId, _, actionSource, time, playerId, bulletId, damage) =>
      PlayerHitByBullet(actionId, playerId, bulletId, damage, time, actionSource)

    case NewHealUnitMessage(actionId, _, actionSource, time, id, xPos, yPos) =>
      NewHealUnit(actionId, time, id, Complex(xPos, yPos), actionSource)

    case DestroyHealUnitMessage(actionId, _, actionSource, time, id) =>
      DestroyHealUnit(actionId, time, id, actionSource)

    case PlayerTakeHealUnitMessage(actionId, _, actionSource, time, playerId, unitId) =>
      PlayerTakeHealUnit(actionId, time, playerId, unitId, actionSource)

    case PlayerTakeDamageMessage(actionId, _, actionSource, time, playerId, sourceId, damage) =>
      PlayerTakeDamage(actionId, time, playerId, sourceId, damage, actionSource)

    case UpdateDamageZoneMessage(actionId, _, actionSource, time, zoneId, lastGrow, lastTick, xPos, yPos, radius) =>
      UpdateDamageZone(actionId, time, zoneId, lastGrow, lastTick, xPos, yPos, radius, actionSource)

    case UpdateMistMessage(actionId, _, actionSource, time, id, lastGrow, lastTick, sideLength, gameAreaSideLength) =>
      UpdateMist(actionId, time, id, lastGrow, lastTick, sideLength, gameAreaSideLength, actionSource)

    case DestroyDamageZoneMessage(actionId, _, actionSource, time, zoneId) =>
      DestroyDamageZone(actionId, time, zoneId, actionSource)

    case GunTurretShootsMessage(actionId, _, actionSource, time,
    turretId, rotation, bulletId, bulletRadius, bulletSpeed) =>
      GunTurretShoots(actionId, time, turretId, rotation, bulletId, bulletRadius, bulletSpeed, actionSource)

    case GunTurretTakesDamageMessage(actionId, _, actionSource, time, turretId, damage) =>
      GunTurretTakesDamage(actionId, time, turretId, damage, actionSource)

    case DestroyGunTurretMessage(actionId, _, actionSource, time, turretId) =>
      DestroyGunTurret(actionId, time, turretId, actionSource)

    case NewGunTurretMessage(actionId, _, actionSource, time, turretId, ownerId, teamId, pos, radius) =>
      NewGunTurret(actionId, time, turretId, ownerId, teamId, pos.toComplex, radius, actionSource)

    case ActionChangerEndedMessage(actionId, _, actionSource, time, changerId) =>
      ActionChangerEnded(actionId, time, changerId, actionSource)

    case NewShieldMessage(actionId, _, actionSource, time, shieldId, playerId) =>
      NewActionChanger(actionId, time, new Shield(shieldId, time, playerId), actionSource)

    case NewBulletGlueMessage(actionId, _, actionSource, time, id, playerId, teamId) =>
      NewActionChanger(actionId, time, new BulletGlue(id, time, playerId, teamId), actionSource)

    case ActivateShieldMessage(actionId, _, actionSource, time, useId, playerId) =>
      UseAbilityAction(actionId, time, new ActivateShield(time, useId, playerId), useId, actionSource)

    case BigBulletMessage(actionId, _, actionSource, time, useId, casterId, teamId, xPos, yPos, direction) =>
      UseAbilityAction(
        actionId, time,
        new BigBullet(time, useId, casterId, teamId, Complex(xPos, yPos), direction),
        useId, actionSource
      )

    case TripleBulletMessage(actionId, _, actionSource, time, useId, casterId, teamId, xPos, yPos, direction) =>
      UseAbilityAction(
        actionId, time,
        new TripleBullet(time, useId, casterId, teamId, Complex(xPos, yPos), direction), useId, actionSource
      )

    case TeleportationMessage(actionId, _, actionSource, time, useId, casterId, startPos, endPos) =>
      UseAbilityAction(
        actionId, time,
        new Teleportation(time, useId, casterId, startPos.toComplex, endPos.toComplex), useId, actionSource
      )

    case CreateHealingZoneMessage(actionId, _, actionSource, time, useId, casterId, targetPos) =>
      UseAbilityAction(actionId, time,
        new CreateHealingZone(time, useId, casterId, targetPos.toComplex), useId, actionSource)

    case LaunchSmashBulletMessage(actionId, _, actionSource, time, useId, casterId, pos, direction) =>
      UseAbilityAction(
        actionId, time, new LaunchSmashBullet(time, useId, casterId, pos.toComplex, direction), useId, actionSource
      )

    case CraftGunTurretMessage(actionId, _, actionSource, time, useId, casterId, teamId, pos) =>
      UseAbilityAction(actionId, time,
        new CraftGunTurret(time, useId, casterId, teamId, pos.toComplex), useId, actionSource)

    case CreateBulletAmplifierMessage(actionId, _, actionSource, time, useId, casterId, targetPos, rotation) =>
      UseAbilityAction(
        actionId, time,
        new CreateBulletAmplifier(time, useId, casterId, targetPos.toComplex, rotation), useId, actionSource
      )

    case CreateBarrierMessage(actionId, _, actionSource, time, useId, casterId, teamId, pos, rotation) =>
      UseAbilityAction(
        actionId, time, new CreateBarrier(time, useId, casterId, teamId, pos.toComplex, rotation), useId, actionSource
      )

    case PutBulletGlueMessage(actionId, _, actionSource, time, useId, casterId, teamId) =>
      UseAbilityAction(
        actionId, time, new PutBulletGlue(time, useId, casterId, teamId), useId, actionSource
      )

    case LaserAbilityMessage(actionId, _, actionSource, time, useId, casterId, teamId, stepNumber, pos) =>
      UseAbilityAction(
        actionId, time, new LaserAbility(time, useId, casterId, teamId, stepNumber, pos.toComplex), useId, actionSource
      )

    case ChangeBulletRadiusMessage(actionId, _, actionSource, time, bulletId, newRadius) =>
      ChangeBulletRadius(actionId, time, bulletId, newRadius, actionSource)

    case TranslatePlayerMessage(actionId, _, actionSource, time, playerId, x, y) =>
      TranslatePlayer(actionId, time, playerId, x, y, actionSource)

    case UpdateHealingZoneMessage(actionId, _, actionSource, time, zoneId, team, lifeSupply, xPos, yPos) =>
      UpdateHealingZone(actionId, time, zoneId, team, lifeSupply, xPos, yPos, actionSource)

    case HealingZoneHealMessage(actionId, _, actionSource, time, healedUnitId, healingZoneId, amount) =>
      HealingZoneHeals(actionId, time, healedUnitId, healingZoneId, amount, actionSource)

    case NewHealingZoneMessage(actionId, _, actionSource, time, zoneId, ownerId, lifeSupply, xPos, yPos) =>
      NewHealingZone(actionId, time, zoneId, ownerId, lifeSupply, xPos, yPos, actionSource)

    case NewLaserLauncherMessage(actionId, _, actionSource, time, laserLauncherId, pos, ownerId) =>
      NewLaserLauncher(actionId, time, laserLauncherId, pos.toComplex, ownerId, actionSource)

    case DestroyLaserLauncherMessage(actionId, _, actionSource, time, laserLauncherId) =>
      DestroyLaserLauncher(actionId, time, laserLauncherId, actionSource)

    case FireLaserMessage(actionId, _, actionSource, time, ownerId, laserVertices) =>
      FireLaser(actionId, time, ownerId, laserVertices.map(_.toComplex), actionSource)

    case DestroyHealingZoneMessage(actionId, _, actionSource, time, zoneId) =>
      DestroyHealingZone(actionId, time, zoneId, actionSource)

    case NewSmashBulletMessage(actionId, _, actionSource, time, bulletId, ownerId, pos, dir, radius, speed) =>
      NewSmashBullet(actionId, time, bulletId, ownerId, pos.toComplex, dir, radius, speed, actionSource)

    case PlayerHitSmashBulletMessage(actionId, _, actionSource, time, playerId, bulletId) =>
      PlayerHitBySmashBullet(actionId, time, playerId, bulletId, actionSource)

    case SmashBulletGrowsMessage(actionId, _, actionSource, time, smashBulletId, newRadius) =>
      SmashBulletGrows(actionId, time, smashBulletId, newRadius, actionSource)

    case DestroySmashBulletMessage(actionId, _, actionSource, time, bulletId) =>
      DestroySmashBullet(actionId, time, bulletId, actionSource)

    case BulletAmplifierAmplifiedMessage(actionId, _, actionSource, time, bulletId, amplifierId) =>
      BulletAmplifierAmplified(actionId, time, bulletId, amplifierId, actionSource)

    case NewBulletAmplifierMessage(actionId, _, actionSource, time, id, ownerId, rotation, pos) =>
      NewBulletAmplifier(actionId, time, id, ownerId, rotation, pos.toComplex, actionSource)

    case DestroyBulletAmplifierMessage(actionId, _, actionSource, time, id) =>
      DestroyBulletAmplifier(actionId, time, id, actionSource)

    case NewBarrierMessage(actionId, _, actionSource, time, id, ownerId, teamId, rotation, pos) =>
      NewBarrier(actionId, time, id, ownerId, teamId, pos.toComplex, rotation, actionSource)

    case DestroyBarrierMessage(actionId, _, actionSource, time, id) =>
      DestroyBarrier(actionId, time, id, actionSource)

    case RemoveRelevantAbilityMessage(actionId, _, actionSource, time, casterId, useId) =>
      RemoveRelevantAbility(actionId, time, casterId, useId, actionSource)

    case NewAbilityGiverMessage(actionId, _, actionSource, time, abilityGiverId, pos, abilityId) =>
      NewAbilityGiver(actionId, time, abilityGiverId, pos.toComplex, abilityId, actionSource)

    case PlayerTakeAbilityGiverMessage(actionId, _, actionSource, time, playerId, abilityGiverId, abilityId) =>
      PlayerTakeAbilityGiver(actionId, time, playerId, abilityGiverId, abilityId, actionSource)

    case PlayerTakesFlagMessage(actionId, _, actionSource, time, flagId, playerId) =>
      PlayerTakesFlag(actionId, time, flagId, playerId, actionSource)

    case PlayerDropsFlagMessage(actionId, _, actionSource, time, flagId) =>
      PlayerDropsFlag(actionId, time, flagId, actionSource)

    case PlayerBringsFlagBackMessage(actionId, _, actionSource, time, flagId, playerId) =>
      PlayerBringsFlagBack(actionId, time, flagId, playerId, actionSource)

    case NewTeamFlagMessage(actionId, _, actionSource, time, flagId, teamNbr, pos) =>
      NewTeamFlag(actionId, time, flagId, teamNbr, pos.toComplex, actionSource)

    case NewObstacleMessage(actionId, _, actionSource, time, id, xPos, yPos, vertices) =>
      NewObstacle(actionId, time, id, Complex(xPos, yPos), vertices.map(elem => Complex(elem.x, elem.y)), actionSource)

    case PlayerDeadMessage(actionId, _, actionSource, time, playerId, playerName) =>
      PlayerDead(actionId, time, playerId, playerName, actionSource)

    case NewPlayerMessage(actionId, _, actionSource, time, name, id, x, y, team, allowedAbilities) =>
      NewPlayerAction(actionId, new Player(
        id, team, time, name, x, y, allowedAbilities = allowedAbilities, relevantUsedAbilities = Map()
      ), time, actionSource)

    case GameBeginsMessage(actionId, _, actionSource, time, vertices) =>
      GameBegins(actionId, time, Polygon(vertices.map(_.toComplex)), actionSource)

    case GameEndedMessage(actionId, _, actionSource, time) =>
      GameEndedAction(actionId, time, actionSource)
  }



  def abilityToMessage(gameName: String, ability: Ability): UseAbility = ability match {
    case ability: ActivateShield =>
      UseActivateShield(gameName, ability.time, ability.useId, ability.playerId)
    case ability: BigBullet =>
      UseBigBullet(
        gameName, ability.time, ability.useId, ability.casterId, ability.teamId,
        ability.startingPos.re, ability.startingPos.im, ability.rotation
      )
    case ability: TripleBullet =>
      UseTripleBullet(
        gameName, ability.time, ability.useId, ability.casterId, ability.teamId,
        ability.startingPos.re, ability.startingPos.im, ability.rotation
      )
    case ability: Teleportation =>
      UseTeleportation(
        gameName, ability.time, ability.useId, ability.casterId, ability.startingPos, ability.endPos
      )
    case ability: CreateHealingZone =>
      UseCreateHealingZone(
        gameName, ability.time, ability.useId, ability.casterId, ability.targetPos
      )
    case ability: CreateBulletAmplifier =>
      UseCreateBulletAmplifier(
        gameName, ability.time, ability.useId, ability.casterId, ability.targetPos, ability.rotation
      )
    case ability: LaunchSmashBullet =>
      UseLaunchSmashBullet(
        gameName, ability.time, ability.useId, ability.casterId, ability.startingPos, ability.rotation
      )
    case ability: CraftGunTurret =>
      UseCraftGunTurret(
        gameName, ability.time, ability.useId, ability.casterId, ability.teamId, ability.pos
      )
    case ability: CreateBarrier =>
      UseCreateBarrier(
        gameName, ability.time, ability.useId, ability.casterId, ability.teamId, ability.targetPos, ability.rotation
      )
    case ability: PutBulletGlue =>
      UsePutBulletGlue(
        gameName, ability.time, ability.useId, ability.casterId, ability.teamId
      )
    case ability: LaserAbility =>
      UseLaser(
        gameName, ability.time, ability.useId, ability.casterId, ability.teamId, ability.stepNumber, ability.pos
      )
    case _ =>
      println(s"You forgot to add ability (${ability.id}) to the case match of abilityToMessage.")
      throw new ForgotToAddCaseMatchException
  }

  def messageToAbility(message: UseAbility): Ability = message match {
    case UseActivateShield(_, time, useId, playerId) =>
      new ActivateShield(time, useId, playerId)
    case UseBigBullet(_, time, useId, casterId, teamId, xPos, yPos, direction) =>
      new BigBullet(time, useId, casterId, teamId, Complex(xPos, yPos), direction)
    case UseTripleBullet(_, time, useId, casterId, teamId, xPos, yPos, direction) =>
      new TripleBullet(time, useId, casterId, teamId, Complex(xPos, yPos), direction)
    case UseTeleportation(_, time, useId, casterId, startPos, endPos) =>
      new Teleportation(time, useId, casterId, startPos.toComplex, endPos.toComplex)
    case UseCreateHealingZone(_, time, useId, casterId, targetPos) =>
      new CreateHealingZone(time, useId, casterId, targetPos.toComplex)
    case UseCreateBulletAmplifier(_, time, useId, casterId, targetPos, rotation) =>
      new CreateBulletAmplifier(time, useId, casterId, targetPos.toComplex, rotation)
    case UseLaunchSmashBullet(_, time, useId, casterId, pos, direction) =>
      new LaunchSmashBullet(time, useId, casterId, pos.toComplex, direction)
    case UseCraftGunTurret(_, time, useId, casterId, teamId, pos) =>
      new CraftGunTurret(time, useId, casterId, teamId, pos.toComplex)
    case UseCreateBarrier(_, time, useId, casterId, teamId, targetPos, rotation) =>
      new CreateBarrier(time, useId, casterId, teamId, targetPos.toComplex, rotation)
    case UsePutBulletGlue(_, time, useId, casterId, teamId) =>
      new PutBulletGlue(time, useId, casterId, teamId)
    case UseLaser(_, time, useId, casterId, teamId, stepNbr, pos) =>
      new LaserAbility(time, useId, casterId, teamId, stepNbr, pos.toComplex)
  }





}
