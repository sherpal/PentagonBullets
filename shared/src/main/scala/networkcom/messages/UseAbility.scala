package networkcom.messages

sealed trait UseAbility extends InGameMessage {
  val time: Long

  val useId: Long
}

final case class UseActivateShield(gameName: String, time: Long, useId: Long, playerId: Long) extends UseAbility
final case class UseBigBullet(gameName: String, time: Long, useId: Long, casterId: Long, teamId: Int,
                              xPos: Double, yPos: Double, direction: Double) extends UseAbility
final case class UseTripleBullet(gameName: String, time: Long, useId: Long, casterId: Long, teamId: Int,
                                 xPos: Double, yPos: Double, direction: Double) extends UseAbility
final case class UseTeleportation(gameName: String, time: Long, useId: Long, casterId: Long,
                                  startPos: Point, endPos: Point) extends UseAbility
final case class UseCreateHealingZone(gameName: String, time: Long, useId: Long, casterId: Long,
                                      targetPos: Point) extends UseAbility
final case class UseCreateBulletAmplifier(gameName: String, time: Long, useId: Long, casterId: Long,
                                          targetPos: Point, rotation: Double) extends UseAbility
final case class UseLaunchSmashBullet(gameName: String, time: Long, useId: Long, casterId: Long,
                                      pos: Point, direction: Double) extends UseAbility
final case class UseCraftGunTurret(gameName: String, time: Long, useId: Long, casterId: Long, teamId: Int, pos: Point)
  extends UseAbility
final case class UseCreateBarrier(gameName: String, time: Long, useId: Long, casterId: Long,
                                  teamId: Int, targetPos: Point, rotation: Double) extends UseAbility
final case class UsePutBulletGlue(gameName: String, time: Long, useId: Long, casterId: Long, teamId: Int)
extends UseAbility


final case class InGameChatMessage(gameName: String, s: String, time: Long, sender: String)
  extends InGameMessage with networkcom.ChatMessageType
