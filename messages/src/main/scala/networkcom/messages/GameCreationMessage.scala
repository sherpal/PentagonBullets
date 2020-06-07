package networkcom.messages

/**
  * These messages are sent before a game starts, while players join a game hosted by someone.
  * The host can also set the settings of the game.
  */
sealed trait GameCreationMessage extends networkcom.Message

final case class CancelGame(gameName: String) extends GameCreationMessage
final case class LeaveGame(gameName: String, playerName: String) extends GameCreationMessage
final case class LaunchGame(gameName: String) extends GameCreationMessage
final case class GameLaunched(gameName: String, password: Int, players: Array[SendPlayerInfo]) //, ids: Array[String])
    extends GameCreationMessage
final case class NewPlayerArrives(gameName: String, playerName: String, reservationId: Int) extends GameCreationMessage
final case class CurrentPlayers(gameId: Long, players: Array[SendPlayerInfo]) extends GameCreationMessage
final case class PlayerReady(gameName: String, playerName: String, status: Boolean) extends GameCreationMessage
final case class ChoseAbility(gameName: String, playerName: String, abilityId: Int) extends GameCreationMessage
final case class DoNotChoseAbility(gameName: String, playerName: String, abilityId: Int) extends GameCreationMessage
final case class ChoseTeam(gameName: String, playerName: String, team: Int) extends GameCreationMessage
final case class SendPlayerInfo(
    gameName: String,
    playerName: String,
    id: Long,
    team: Int,
    ready: Boolean,
    abilities: List[Int],
    color: Vector[Double]
) extends GameCreationMessage

final case class GameCreationChatMessage(gameName: String, s: String, time: Long, sender: String)
    extends GameCreationMessage
    with networkcom.ChatMessageType
