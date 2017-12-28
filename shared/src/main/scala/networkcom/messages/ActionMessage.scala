package networkcom.messages

sealed trait ActionMessage extends InGameMessage {
  val actionId: Long

  val time: Long

  val actionSource: String
}


final case class GameBeginsMessage(
                                    actionId: Long,
                                    gameName: String,
                                    actionSource: String,
                                    time: Long,
                                    vertices: Vector[Point]
                                  ) extends ActionMessage

final case class GameEndedMessage(
                                   actionId: Long,
                                   gameName: String,
                                   actionSource: String,
                                   time: Long
                                 ) extends ActionMessage

// we will for now assume that every player will have the same shape, speed and life total.
final case class NewPlayerMessage(
                                   actionId: Long,
                                   gameName: String,
                                   actionSource: String,
                                   time: Long,
                                   playerName: String,
                                   id: Long,
                                   x: Double, y: Double,
                                   team: Int,
                                   abilities: List[Int]
                                 ) extends ActionMessage

final case class UpdatePlayerPosMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         id: Long,
                                         x: Double, y: Double,
                                         dir: Double, moving: Boolean,
                                         rot: Double
                                       ) extends ActionMessage

final case class TranslatePlayerMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         playerId: Long,
                                         x: Double, y: Double
                                       ) extends ActionMessage

final case class NewBulletMessage(
                                   actionId: Long,
                                   gameName: String,
                                   actionSource: String,
                                   time: Long,
                                   id: Long,
                                   plrId: Long,
                                   teamId: Int,
                                   x: Double, y: Double,
                                   radius: Int, dir: Double,
                                   speed: Int,
                                   travelledDistance: Double
                                 ) extends ActionMessage

final case class PlayerHitMessage(
                                   actionId: Long,
                                   gameName: String,
                                   actionSource: String,
                                   time: Long,
                                   playerId: Long,
                                   bulletId: Long,
                                   damage: Double
                                 ) extends ActionMessage

final case class PlayerHitByBulletsMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            bulletIds: List[Long],
                                            playerId: Long,
                                            totalDamage: Double
                                          ) extends ActionMessage

final case class DestroyBulletMessage(
                                       actionId: Long,
                                       gameName: String,
                                       actionSource: String,
                                       time: Long,
                                       id: Long
                                     ) extends ActionMessage

final case class NewSmashBulletMessage(
                                        actionId: Long,
                                        gameName: String,
                                        actionSource: String,
                                        time: Long,
                                        bulletId: Long,
                                        ownerId: Long,
                                        pos: Point, dir: Double,
                                        radius: Int,
                                        speed: Double
                                      ) extends ActionMessage

final case class SmashBulletGrowsMessage(
                                          actionId: Long,
                                          gameName: String,
                                          actionSource: String,
                                          time: Long,
                                          smashBulletId: Long,
                                          newRadius: Int
                                        ) extends ActionMessage

final case class PlayerHitSmashBulletMessage(
                                              actionId: Long,
                                              gameName: String,
                                              actionSource: String,
                                              time: Long,
                                              playerId: Long,
                                              bulletId: Long
                                            ) extends ActionMessage

final case class DestroySmashBulletMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            bulletId: Long
                                          ) extends ActionMessage

final case class NewObstacleMessage(
                                     actionId: Long,
                                     gameName: String,
                                     actionSource: String,
                                     time: Long,
                                     id: Long,
                                     xPos: Double,
                                     yPos: Double,
                                     vertices: Vector[Point]
                                   ) extends ActionMessage

final case class NewHealUnitMessage(
                                     actionId: Long,
                                     gameName: String,
                                     actionSource: String,
                                     time: Long,
                                     id: Long,
                                     xPos: Double, yPos: Double
                                   ) extends ActionMessage

final case class DestroyHealUnitMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         unitId: Long
                                       ) extends ActionMessage

final case class PlayerTakeHealUnitMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            playerId: Long,
                                            unitId: Long
                                          ) extends ActionMessage

final case class NewAbilityGiverMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         abilityGiverId: Long,
                                         pos: Point,
                                         abilityId: Int
                                       ) extends ActionMessage

final case class PlayerTakeAbilityGiverMessage(
                                                actionId: Long,
                                                gameName: String,
                                                actionSource: String,
                                                time: Long,
                                                playerId: Long,
                                                abilityGiverId: Long,
                                               abilityId: Int
                                              ) extends ActionMessage

final case class PlayerDeadMessage(
                                    actionId: Long,
                                    gameName: String,
                                    actionSource: String,
                                    time: Long,
                                    playerId: Long,
                                    playerName: String) extends ActionMessage

final case class UpdateDamageZoneMessage(
                                          actionId: Long,
                                          gameName: String,
                                          actionSource: String,
                                          time: Long,
                                          zoneId: Long,
                                          lastGrow: Long,
                                          lastTick: Long,
                                          xPos: Double,
                                          yPos: Double,
                                          radius: Int
                                        ) extends ActionMessage

final case class DestroyDamageZoneMessage(
                                           actionId: Long,
                                           gameName: String,
                                           actionSource: String,
                                           time: Long,
                                           zoneId: Long
                                         ) extends ActionMessage

final case class PlayerTakeDamageMessage(
                                          actionId: Long,
                                          gameName: String,
                                          actionSource: String,
                                          time: Long,
                                          playerId: Long,
                                          sourceId: Long,
                                          damage: Double
                                        ) extends ActionMessage

final case class NewGunTurretMessage(
                                      actionId: Long,
                                      gameName: String,
                                      actionSource: String,
                                      time: Long,
                                      turretId: Long,
                                      ownerId: Long,
                                      teamId: Int,
                                      pos: Point,
                                      radius: Double
                                    ) extends ActionMessage

final case class GunTurretShootsMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         turretId: Long,
                                         rotation: Double,
                                         bulletId: Long,
                                         bulletRadius: Int,
                                         bulletSpeed: Double
                                       ) extends ActionMessage

final case class GunTurretTakesDamageMessage(
                                              actionId: Long,
                                              gameName: String,
                                              actionSource: String,
                                              time: Long,
                                              turretId: Long,
                                              damage: Double
                                            ) extends ActionMessage

final case class DestroyGunTurretMessage(
                                          actionId: Long,
                                          gameName: String,
                                          actionSource: String,
                                          time: Long,
                                          turretId: Long
                                        ) extends ActionMessage

final case class ActionChangerEndedMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            changerId: Long
                                          ) extends ActionMessage

final case class NewShieldMessage(
                                   actionId: Long,
                                   gameName: String,
                                   actionSource: String,
                                   time: Long,
                                   shieldId: Long,
                                   playerId: Long
                                 ) extends ActionMessage

final case class NewBulletGlueMessage(
                                       actionId: Long,
                                       gameName: String,
                                       actionSource: String,
                                       time: Long,
                                       id: Long,
                                       playerId: Long,
                                       teamId: Int
                                     ) extends ActionMessage

final case class ActivateShieldMessage(
                                        actionId: Long,
                                        gameName: String,
                                        actionSource: String,
                                        time: Long,
                                        useId: Long,
                                        playerId: Long
                                      ) extends ActionMessage

final case class BigBulletMessage(
                                   actionId: Long,
                                   gameName: String,
                                   actionSource: String,
                                   time: Long,
                                   useId: Long,
                                   casterId: Long,
                                   teamId: Int,
                                   xPos: Double, yPos: Double,
                                   direction: Double
                                 ) extends ActionMessage

final case class TripleBulletMessage(
                                      actionId: Long,
                                      gameName: String,
                                      actionSource: String,
                                      time: Long,
                                      useId: Long,
                                      casterId: Long,
                                      teamId: Int,
                                      xPos: Double, yPos: Double,
                                      direction: Double
                                    ) extends ActionMessage

final case class TeleportationMessage(
                                       actionId: Long,
                                       gameName: String,
                                       actionSource: String,
                                       time: Long,
                                       useId: Long,
                                       casterId: Long,
                                       startPos: Point,
                                       endPos: Point
                                     ) extends ActionMessage

final case class CreateHealingZoneMessage(
                                           actionId: Long,
                                           gameName: String,
                                           actionSource: String,
                                           time: Long,
                                           useId: Long,
                                           casterId: Long,
                                           targetPos: Point
                                         ) extends ActionMessage

final case class CreateBulletAmplifierMessage(
                                               actionId: Long,
                                               gameName: String,
                                               actionSource: String,
                                               time: Long,
                                               useId: Long,
                                               casterId: Long,
                                               targetPos: Point,
                                               rotation: Double
                                             ) extends ActionMessage

final case class LaunchSmashBulletMessage(
                                           actionId: Long,
                                           gameName: String,
                                           actionSource: String,
                                           time: Long,
                                           useId: Long,
                                           casterId: Long,
                                           pos: Point,
                                           direction: Double
                                         ) extends ActionMessage

final case class CraftGunTurretMessage(
                                        actionId: Long,
                                        gameName: String,
                                        actionSource: String,
                                        time: Long,
                                        useId: Long,
                                        casterId: Long,
                                        teamId: Int,
                                        pos: Point) extends ActionMessage

final case class CreateBarrierMessage(
                                       actionId: Long,
                                       gameName: String,
                                       actionSource: String,
                                       time: Long,
                                       useId: Long,
                                       casterId: Long,
                                       teamId: Int,
                                       pos: Point,
                                       rotation: Double
                                     ) extends ActionMessage

final case class PutBulletGlueMessage(
                                       actionId: Long,
                                       gameName: String,
                                     actionSource: String,
                                     time: Long,
                                     useId: Long,
                                     casterId: Long,
                                     teamId: Int
                                     ) extends ActionMessage

final case class LaserAbilityMessage(
                                      actionId: Long,
                                      gameName: String,
                                      actionSource: String,
                                      time: Long,
                                      useId: Long,
                                      casterId: Long,
                                      teamId: Int,
                                      stepNumber: Int,
                                      pos: Point
                                    ) extends ActionMessage

final case class FireLaserMessage(
                                  actionId: Long,
                                  gameName: String,
                                  actionSource: String,
                                  time: Long,
                                  ownerId: Long,
                                  pos1: Point,
                                  pos2: Point
                                 ) extends ActionMessage

final case class ChangeBulletRadiusMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            bulletId: Long,
                                            newRadius: Int
                                          ) extends ActionMessage

final case class UpdateMistMessage(
                                    actionId: Long,
                                    gameName: String,
                                    actionSource: String,
                                    time: Long,
                                    id: Long,
                                    lastGrow: Long,
                                    lastTick: Long,
                                    sideLength: Double,
                                    gameAreaSideLength: Int
                                  ) extends ActionMessage

final case class HealingZoneHealMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         healedUnitId: Long,
                                         healingZoneId: Long,
                                         amount: Double
                                       ) extends ActionMessage

final case class NewHealingZoneMessage(
                                        actionId: Long,
                                        gameName: String,
                                        actionSource: String,
                                        time: Long,
                                        zoneId: Long,
                                        ownerId: Long,
                                        lifeSupply: Double,
                                        xPos: Double, yPos: Double
                                      ) extends ActionMessage

final case class UpdateHealingZoneMessage(
                                           actionId: Long,
                                           gameName: String,
                                           actionSource: String,
                                           time: Long,
                                           zoneId: Long,
                                           ownerId: Long,
                                           lifeSupply: Double,
                                           xPos: Double, yPos: Double
                                         ) extends ActionMessage

final case class DestroyHealingZoneMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            zoneId: Long
                                          ) extends ActionMessage

final case class NewBulletAmplifierMessage(
                                            actionId: Long,
                                            gameName: String,
                                            actionSource: String,
                                            time: Long,
                                            id: Long,
                                            ownerId: Long,
                                            rotation: Double,
                                            pos: Point
                                          ) extends ActionMessage

final case class DestroyBulletAmplifierMessage(
                                                actionId: Long,
                                                gameName: String,
                                                actionSource: String,
                                                time: Long,
                                                id: Long
                                              ) extends ActionMessage

final case class BulletAmplifierAmplifiedMessage(
                                                  actionId: Long,
                                                  gameName: String,
                                                  actionSource: String,
                                                  time: Long,
                                                  bulletId: Long,
                                                  amplifierId: Long
                                                ) extends ActionMessage

final case class NewBarrierMessage(
                                    actionId: Long,
                                    gameName: String,
                                    actionSource: String,
                                    time: Long,
                                    id: Long,
                                    ownerId: Long,
                                    teamId: Int,
                                    rotation: Double,
                                    pos: Point
                                  ) extends ActionMessage

final case class DestroyBarrierMessage(
                                        actionId: Long,
                                        gameName: String,
                                        actionSource: String,
                                        time: Long,
                                        id: Long
                                      ) extends ActionMessage

final case class RemoveRelevantAbilityMessage(
                                               actionId: Long,
                                               gameName: String,
                                               actionSource: String,
                                               time: Long,
                                               casterId: Long,
                                               useId: Long
                                             ) extends ActionMessage


final case class NewLaserLauncherMessage(
                                          actionId: Long,
                                          gameName: String,
                                          actionSource: String,
                                          time: Long,
                                          laserLauncherId: Long,
                                          pos: Point,
                                          ownerId: Long
                                        ) extends ActionMessage

final case class DestroyLaserLauncherMessage(
                                              actionId: Long,
                                              gameName: String,
                                              actionSource: String,
                                              time: Long,
                                              laserLauncherId: Long
                                            ) extends ActionMessage


/** Capture the Flag */
final case class NewTeamFlagMessage(
                                     actionId: Long,
                                     gameName: String,
                                     actionSource: String,
                                     time: Long,
                                     flagId: Long,
                                     teamNbr: Int,
                                     pos: Point
                                   ) extends ActionMessage

final case class PlayerTakesFlagMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         flagId: Long,
                                         playerId: Long
                                       ) extends ActionMessage

final case class PlayerDropsFlagMessage(
                                         actionId: Long,
                                         gameName: String,
                                         actionSource: String,
                                         time: Long,
                                         flagId: Long
                                       ) extends ActionMessage

final case class PlayerBringsFlagBackMessage(
                                              actionId: Long,
                                              gameName: String,
                                              actionSource: String,
                                              time: Long,
                                              flagId: Long,
                                              playerId: Long
                                            ) extends ActionMessage
