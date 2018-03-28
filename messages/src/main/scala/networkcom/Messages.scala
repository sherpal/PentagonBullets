package networkcom

import boopickle.Default._
import boopickle.CompositePickler
import java.nio.ByteBuffer

import networkcom.messages._
import networkcom.tablemessages._

abstract class Message


object Message {

  def decode(buffer: Array[Byte]): Message =
    Unpickle[Message](messagePickler).fromBytes(ByteBuffer.wrap(buffer))

  def encode(message: Message): Array[Byte] = {
    val byteBuffer = Pickle.intoBytes(message)
    val array = new Array[Byte](byteBuffer.remaining())
    byteBuffer.get(array)
    array
  }


  implicit val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[Connect]
    .addConcreteType[Connected]
    .addConcreteType[Disconnect]
    .addConcreteType[Disconnected]
    .addConcreteType[TestMessage]
    .addConcreteType[TestSendArray]

    /**
     * Tables messages
     */
    .addConcreteType[Table]
    .addConcreteType[Tables]
    .addConcreteType[OpenTable]
    .addConcreteType[TableDestroyed]
    .addConcreteType[TableOpened]
    .addConcreteType[AskTables]
    .addConcreteType[JoinTable]
    .addConcreteType[TableJoined]
    .addConcreteType[LeaveTable]
    .addConcreteType[LaunchGameFromTable]
    .addConcreteType[StandardTableInfoMessage]
    .addConcreteType[LaunchGameError]
    .addConcreteType[ClientCreated]
    .addConcreteType[HolePunching]
    .addConcreteType[CreateServer]
    .addConcreteType[CreateClient]
    .addConcreteType[StandardTableAllInfoMessage]
    .addConcreteType[AskTableInfo]
    .addConcreteType[PlayerPeers]
    .addConcreteType[Hello]


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
    .addConcreteType[NewBulletGlueMessage]

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

