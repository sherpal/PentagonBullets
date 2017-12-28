package networkcom

import boopickle.Default._
import boopickle.CompositePickler

import networkcom.messages._

abstract class Message


object Message {
  implicit val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[Connect]
    .addConcreteType[Connected]
    .addConcreteType[Disconnect]
    .addConcreteType[Disconnected]
    .addConcreteType[TestMessage]
    .addConcreteType[TestSendArray]

    .addConcreteType[ChatMessage]

    .addConcreteType[NewGameCreation]
    .addConcreteType[GameCreated]
    .addConcreteType[GameWasNotCreated]
    .addConcreteType[ReservePlayerName]
    .addConcreteType[PlayerNameReserved]
    .addConcreteType[ReserveGameName]
    .addConcreteType[GameNameReserved]
    .addConcreteType[GameDoesNotExist]

    .addConcreteType[CancelGame]
    .addConcreteType[LeaveGame]
    .addConcreteType[LaunchGame]
    .addConcreteType[GameLaunched]
    .addConcreteType[NewPlayerArrives]
    .addConcreteType[CurrentPlayers]
    .addConcreteType[PlayerReady]
    .addConcreteType[ChoseAbility]
    .addConcreteType[DoNotChoseAbility]
    .addConcreteType[ChoseTeam]
    .addConcreteType[SendPlayerInfo]

    .addConcreteType[GameCreationChatMessage]

    .addConcreteType[PlayerConnecting]
    .addConcreteType[StillWaitingForPlayers]
    .addConcreteType[GameStarts]
    .addConcreteType[GameStartsIn]
    .addConcreteType[GuessClockTime]
    .addConcreteType[AnswerGuessClockTime]

    .addConcreteType[InGameChatMessage]

    .addConcreteType[Point]
    .addConcreteType[ShapeMessage]
    .addConcreteType[CircleMessage]
    .addConcreteType[PolygonMessage]

    .addConcreteType[ActionMessage]
    .addConcreteType[ActionsMessage]
    .addConcreteType[ActionDenied]
    .addConcreteType[ActionsDenied]
    .addConcreteType[DeleteActions]

    .addConcreteType[GameBeginsMessage]
    .addConcreteType[NewPlayerMessage]
    .addConcreteType[UpdatePlayerPosMessage]
    .addConcreteType[TranslatePlayerMessage]
    .addConcreteType[NewBulletMessage]
    .addConcreteType[PlayerHitMessage]
    .addConcreteType[PlayerHitByBulletsMessage]
    .addConcreteType[DestroyBulletMessage]

    .addConcreteType[NewSmashBulletMessage]
    .addConcreteType[SmashBulletGrowsMessage]
    .addConcreteType[PlayerHitSmashBulletMessage]
    .addConcreteType[DestroySmashBulletMessage]

    .addConcreteType[NewObstacleMessage]
    .addConcreteType[NewHealUnitMessage]
    .addConcreteType[DestroyHealUnitMessage]
    .addConcreteType[PlayerTakeHealUnitMessage]
    .addConcreteType[NewAbilityGiverMessage]
    .addConcreteType[PlayerTakeAbilityGiverMessage]
    .addConcreteType[PlayerDeadMessage]
    .addConcreteType[UpdateDamageZoneMessage]
    .addConcreteType[DestroyDamageZoneMessage]
    .addConcreteType[PlayerTakeDamageMessage]

    .addConcreteType[NewGunTurretMessage]
    .addConcreteType[GunTurretShootsMessage]
    .addConcreteType[GunTurretTakesDamageMessage]
    .addConcreteType[DestroyGunTurretMessage]

    .addConcreteType[ActionChangerEndedMessage]
    .addConcreteType[NewShieldMessage]

    .addConcreteType[ActivateShieldMessage]
    .addConcreteType[BigBulletMessage]
    .addConcreteType[TripleBulletMessage]
    .addConcreteType[TeleportationMessage]
    .addConcreteType[CreateHealingZoneMessage]
    .addConcreteType[CreateBulletAmplifierMessage]
    .addConcreteType[LaunchSmashBulletMessage]
    .addConcreteType[CraftGunTurretMessage]
    .addConcreteType[CreateBarrierMessage]
    .addConcreteType[PutBulletGlueMessage]
    .addConcreteType[LaserAbilityMessage]
    .addConcreteType[ChangeBulletRadiusMessage]
    .addConcreteType[UpdateMistMessage]
    .addConcreteType[HealingZoneHealMessage]
    .addConcreteType[NewHealingZoneMessage]
    .addConcreteType[UpdateHealingZoneMessage]
    .addConcreteType[DestroyHealingZoneMessage]
    .addConcreteType[NewBulletAmplifierMessage]
    .addConcreteType[DestroyBulletAmplifierMessage]
    .addConcreteType[BulletAmplifierAmplifiedMessage]
    .addConcreteType[FireLaserMessage]

    .addConcreteType[NewBarrierMessage]
    .addConcreteType[DestroyBarrierMessage]

    .addConcreteType[RemoveRelevantAbilityMessage]

    .addConcreteType[DestroyLaserLauncherMessage]
    .addConcreteType[NewLaserLauncherMessage]

    .addConcreteType[NewTeamFlagMessage]
    .addConcreteType[PlayerTakesFlagMessage]
    .addConcreteType[PlayerDropsFlagMessage]
    .addConcreteType[PlayerBringsFlagBackMessage]

    .addConcreteType[GameEndedMessage]

    .addConcreteType[UseAbility]
    .addConcreteType[UseActivateShield]
    .addConcreteType[UseBigBullet]
    .addConcreteType[UseTripleBullet]
    .addConcreteType[UseTeleportation]
    .addConcreteType[UseCreateHealingZone]
    .addConcreteType[UseCreateBulletAmplifier]
    .addConcreteType[UseLaunchSmashBullet]
    .addConcreteType[UseCraftGunTurret]
    .addConcreteType[UseCreateBarrier]
    .addConcreteType[UsePutBulletGlue]
    .addConcreteType[UseLaser]

    .addConcreteType[ClosingGame]

}

final case class Connect() extends Message
final case class Connected() extends Message
final case class Disconnect() extends Message
final case class Disconnected() extends Message
final case class TestMessage(s: String) extends Message
final case class TestSendArray(strings: Array[String]) extends Message
final case class ChatMessage(s: String, time: Long, sender: String) extends Message


trait ChatMessageType extends Message {
  val gameName: String
  val time: Long
  val s: String
  val sender: String
}

///**
// * These messages are sent in the GameMenu, while someone wants either to host a game, or to join one.
// */
//trait PreGameMessage extends Message
//final case class NewGameCreation(gameName: String, hostName: String, registrationId: Int, gameMode: String)
//  extends PreGameMessage
//final case class GameCreated(gameName: String, id: Long) extends PreGameMessage
//final case class GameWasNotCreated(gameName: String) extends PreGameMessage
//final case class ReservePlayerName(gameName: String, playerName: String) extends PreGameMessage
//final case class PlayerNameReserved(gameName: String, gameMode: String, id: Int, errorMessage: Option[String])
//  extends PreGameMessage
//final case class ReserveGameName(gameName: String, gameMode: String) extends PreGameMessage
//final case class GameNameReserved(gameName: String, gameMode: String, id: Int, errorMessage: Option[String])
//  extends PreGameMessage
//final case class GameDoesNotExist(gameName: String) extends PreGameMessage
//
///**
// * These messages are sent before a game starts, while players join a game hosted by someone.
// * The host can also set the settings of the game.
// */
//trait GameCreationMessage extends Message
//final case class CancelGame(gameName: String) extends GameCreationMessage
//final case class LeaveGame(gameName: String, playerName: String) extends GameCreationMessage
//final case class LaunchGame(gameName: String) extends GameCreationMessage
//final case class GameLaunched(gameName: String, password: Int, players: Array[SendPlayerInfo])//, ids: Array[String])
//  extends GameCreationMessage
//final case class NewPlayer(gameName: String, playerName: String, reservationId: Int) extends GameCreationMessage
//final case class CurrentPlayers(gameId: Long, players: Array[SendPlayerInfo]) extends GameCreationMessage
//final case class PlayerReady(gameName: String, playerName: String, status: Boolean) extends GameCreationMessage
//final case class ChoseAbility(gameName: String, playerName: String, abilityId: Int) extends GameCreationMessage
//final case class DoNotChoseAbility(gameName: String, playerName: String, abilityId: Int) extends GameCreationMessage
//final case class ChoseTeam(gameName: String, playerName: String, team: Int) extends GameCreationMessage
//final case class SendPlayerInfo(gameName: String,
//                                playerName: String,
//                                id: Long,
//                                team: Int,
//                                ready: Boolean,
//                                abilities: List[Int]) extends GameCreationMessage
//final case class GameCreationChatMessage(gameName: String, s: String, time: Long, sender: String)
//  extends GameCreationMessage with ChatMessageType
//
///**
// * These messages are sent during the game.
// * Either the server tells the clients something happen, or the clients communicate their actions.
// */
//trait InGameMessage extends Message {
//  val gameName: String
//}
//final case class PlayerConnecting(gameName: String, playerName: String, password: Int) extends InGameMessage
//final case class StillWaitingForPlayers(gameName: String, n: Int) extends InGameMessage
//final case class GameStarts(gameName: String) extends InGameMessage
//final case class ClosingGame(gameName: String, msg: String) extends InGameMessage
//
//final case class GuessClockTime(gameName: String, time: Long) extends InGameMessage
//final case class AnswerGuessClockTime(gameName: String, guessed: Long, actual: Long) extends InGameMessage
//
//sealed trait ActionMessage extends InGameMessage {
//  val time: Long
//}
//
//// we will for now assume that every player will have the same shape, speed and life total.
//final case class GameBeginsMessage(gameName: String, time: Long, vertices: Vector[Point]) extends ActionMessage
//final case class NewPlayerMessage(gameName: String, time: Long, playerName: String, id: Long,
//                                  x: Double, y: Double, team: Int, abilities: List[Int]) extends ActionMessage
//final case class UpdatePlayerPosMessage(gameName: String, time: Long, id: Long,
//                                       x: Double, y: Double,
//                                       dir: Double, moving: Boolean,
//                                       rot: Double) extends ActionMessage
//final case class TranslatePlayerMessage(gameName: String, time: Long, playerId: Long, x: Double, y: Double)
//  extends ActionMessage
//final case class NewBulletMessage(gameName: String, time: Long, id: Long, plrId: Long,
//                                  x: Double, y: Double, radius: Int, dir: Double) extends ActionMessage
//final case class PlayerHitMessage(gameName: String, time: Long, playerId: Long, bulletId: Long, damage: Double)
//  extends ActionMessage
//final case class DestroyBulletMessage(gameName: String, time: Long, id: Long) extends ActionMessage
//
//final case class NewSmashBulletMessage(gameName: String, time: Long, bulletId: Long, ownerId: Long,
//                                       pos: Point, dir: Double, radius: Int, speed: Double) extends ActionMessage
//final case class PlayerHitSmashBulletMessage(gameName: String, time: Long, playerId: Long, bulletId: Long)
//  extends ActionMessage
//final case class DestroySmashBulletMessage(gameName: String, time: Long, bulletId: Long) extends ActionMessage
//
//final case class NewObstacleMessage(gameName: String, time: Long, id: Long,
//                                    xPos: Double, yPos: Double,
//                                    vertices: Vector[Point]) extends ActionMessage
//final case class NewHealUnitMessage(gameName: String, time: Long, id: Long,
//                                    xPos: Double, yPos: Double) extends ActionMessage
//final case class DestroyHealUnitMessage(gameName: String, time: Long, unitId: Long) extends ActionMessage
//final case class PlayerTakeHealUnitMessage(gameName: String, time: Long, playerId: Long, unitId: Long)
//  extends ActionMessage
//final case class NewAbilityGiverMessage(gameName: String, time: Long, abilityGiverId: Long,
//                                        pos: Point, abilityId: Int) extends ActionMessage
//final case class PlayerTakeAbilityGiverMessage(gameName: String, time: Long, playerId: Long, abilityGiverId: Long,
//                                               abilityId: Int) extends ActionMessage
//final case class PlayerDeadMessage(gameName: String, time: Long, playerId: Long, playerName: String)
//  extends ActionMessage
//final case class UpdateDamageZoneMessage(gameName: String, time: Long, zoneId: Long, lastGrow: Long, lastTick: Long,
//                                  xPos: Double, yPos: Double, radius: Int) extends ActionMessage
//final case class DestroyDamageZoneMessage(gameName: String, time: Long, zoneId: Long) extends ActionMessage
//final case class PlayerTakeDamageMessage(gameName: String, time: Long, playerId: Long,
//                                         sourceId: Long, damage: Double) extends ActionMessage
//
//final case class NewGunTurretMessage(gameName: String, time: Long, turretId: Long, ownerId: Long,
//                                     pos: Point, radius: Double) extends ActionMessage
//final case class GunTurretShootsMessage(gameName: String, time: Long, turretId: Long, rotation: Double,
//                                         bulletId: Long, bulletRadius: Int, bulletSpeed: Double) extends ActionMessage
//final case class GunTurretTakesDamageMessage(gameName: String, time: Long, turretId: Long, damage: Double)
//  extends ActionMessage
//final case class DestroyGunTurretMessage(gameName: String, time: Long, turretId: Long) extends ActionMessage
//
//final case class ActionChangerEndedMessage(gameName: String, time: Long, changerId: Long) extends ActionMessage
//final case class NewShieldMessage(gameName: String, time: Long, shieldId: Long, playerId: Long) extends ActionMessage
//
//final case class ActivateShieldMessage(gameName: String, time: Long, useId: Long, playerId: Long) extends ActionMessage
//final case class BigBulletMessage(gameName: String, time: Long, useId: Long, casterId: Long,
//                                  xPos: Double, yPos: Double, direction: Double) extends ActionMessage
//final case class TripleBulletMessage(gameName: String, time: Long, useId: Long, casterId: Long,
//                                     xPos: Double, yPos: Double, direction: Double) extends ActionMessage
//final case class TeleportationMessage(gameName: String, time: Long, useId: Long, casterId: Long,
//                                      startPos: Point, endPos: Point) extends ActionMessage
//final case class CreateHealingZoneMessage(gameName: String, time: Long, useId: Long, casterId: Long,
//                                          targetPos: Point) extends ActionMessage
//final case class CreateBulletAmplifierMessage(gameName: String, time: Long, useId: Long, casterId: Long,
//                                              targetPos: Point, rotation: Double) extends ActionMessage
//final case class LaunchSmashBulletMessage(gameName: String, time: Long, useId: Long, casterId: Long,
//                                          pos: Point, direction: Double) extends ActionMessage
//final case class CraftGunTurretMessage(gameName: String, time: Long, useId: Long, casterId: Long, pos: Point)
//  extends ActionMessage
//
//final case class ChangeBulletRadiusMessage(gameName: String, time: Long, bulletId: Long, newRadius: Int)
//  extends ActionMessage
//
//final case class UpdateMistMessage(gameName: String, time: Long, id: Long, lastGrow: Long, lastTick: Long,
//                                   sideLength: Double, gameAreaSideLength: Int) extends ActionMessage
//
//final case class HealingZoneHealMessage(gameName: String, time: Long, healedUnitId: Long, healingZoneId: Long,
//                                        amount: Double) extends ActionMessage
//final case class NewHealingZoneMessage(gameName: String, time: Long, zoneId: Long, ownerId: Long, lifeSupply: Double,
//                                       xPos: Double, yPos: Double) extends ActionMessage
//final case class UpdateHealingZoneMessage(gameName: String, time: Long, zoneId: Long, ownerId: Long, lifeSupply: Double,
//                                          xPos: Double, yPos: Double) extends ActionMessage
//final case class DestroyHealingZoneMessage(gameName: String, time: Long, zoneId: Long) extends ActionMessage
//final case class NewBulletAmplifierMessage(gameName: String, time: Long, id: Long, ownerId: Long,
//                                           rotation: Double, pos: Point) extends ActionMessage
//final case class DestroyBulletAmplifierMessage(gameName: String, time: Long, id: Long) extends ActionMessage
//final case class BulletAmplifierAmplifiedMessage(gameName: String, time: Long, bulletId: Long, amplifierId: Long)
//  extends ActionMessage
//
//final case class RemoveRelevantAbilityMessage(gameName: String, time: Long, casterId: Long, useId: Long)
//  extends ActionMessage
//
///**
// * Capture the Flag
// */
//
//final case class NewTeamFlagMessage(gameName: String, time: Long, flagId: Long, teamNbr: Int, pos: Point)
//  extends ActionMessage
//final case class PlayerTakesFlagMessage(gameName: String, time: Long, flagId: Long, playerId: Long)
//  extends ActionMessage
//final case class PlayerDropsFlagMessage(gameName: String, time: Long, flagId: Long) extends ActionMessage
//final case class PlayerBringsFlagBackMessage(gameName: String, time: Long, flagId: Long, playerId: Long)
//  extends ActionMessage
//
///**
// * Complex translator.
// */
//final case class Point(x: Double, y: Double) extends Message {
//  def toComplex: Complex = Complex(x, y)
//}
//
///** Shape translator. */
//sealed trait ShapeMessage extends Message
//object ShapeMessage {
//  def shapeToMessage(shape: Shape): ShapeMessage = shape match {
//    case shape: Circle => CircleMessage(shape.radius)
//    case shape: ConvexPolygon => PolygonMessage(shape.vertices.map(z => Point(z.re, z.im)), convex = true)
//    case shape: NonConvexPolygon => PolygonMessage(shape.vertices.map(z => Point(z.re, z.im)), convex = false)
//    case _: MonotonePolygon => throw new NotImplementedError("Monotone Polygons are not implemented yet.")
//      // because I never use them.
//  }
//
//  def messageToShape(message: ShapeMessage): Shape = message match {
//    case CircleMessage(radius) => new Circle(radius)
//    case PolygonMessage(verticesPoints, isConvex) => Polygon(verticesPoints.map(_.toComplex), isConvex)
//  }
//}
//final case class CircleMessage(radius: Double) extends ShapeMessage
//final case class PolygonMessage(vertices: Vector[Point], convex: Boolean) extends ShapeMessage
//
//
//
//
//final case class GameEndedMessage(gameName: String, time: Long) extends ActionMessage
//
//final case class ActionsMessage(gameName: String, actions: List[ActionMessage]) extends InGameMessage
//final case class ActionDenied(gameName: String, action: ActionMessage) extends InGameMessage
//final case class ActionsDenied(gameName: String, actions: List[ActionMessage]) extends InGameMessage
//
//
//sealed trait UseAbility extends InGameMessage {
//  val time: Long
//
//  val useId: Long
//}
//
//final case class UseActivateShield(gameName: String, time: Long, useId: Long, playerId: Long) extends UseAbility
//final case class UseBigBullet(gameName: String, time: Long, useId: Long, casterId: Long,
//                              xPos: Double, yPos: Double, direction: Double) extends UseAbility
//final case class UseTripleBullet(gameName: String, time: Long, useId: Long, casterId: Long,
//                                 xPos: Double, yPos: Double, direction: Double) extends UseAbility
//final case class UseTeleportation(gameName: String, time: Long, useId: Long, casterId: Long,
//                                  startPos: Point, endPos: Point) extends UseAbility
//final case class UseCreateHealingZone(gameName: String, time: Long, useId: Long, casterId: Long,
//                                      targetPos: Point) extends UseAbility
//final case class UseCreateBulletAmplifier(gameName: String, time: Long, useId: Long, casterId: Long,
//                                          targetPos: Point, rotation: Double) extends UseAbility
//final case class UseLaunchSmashBullet(gameName: String, time: Long, useId: Long, casterId: Long,
//                                      pos: Point, direction: Double) extends UseAbility
//final case class UseCraftGunTurret(gameName: String, time: Long, useId: Long, casterId: Long, pos: Point)
//  extends UseAbility
//
//
//final case class InGameChatMessage(gameName: String, s: String, time: Long, sender: String)
//  extends InGameMessage with ChatMessageType
//
