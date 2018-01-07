package networkcom.messages

/**
 * These messages are sent in the GameMenu, while someone wants either to host a game, or to join one.
 */
sealed trait PreGameMessage extends networkcom.Message

final case class NewGameCreation(gameName: String, hostName: String, registrationId: Int, gameMode: String)
  extends PreGameMessage
final case class GameCreated(gameName: String, id: Long) extends PreGameMessage
final case class GameWasNotCreated(gameName: String) extends PreGameMessage
final case class ReservePlayerName(gameName: String, playerName: String) extends PreGameMessage
final case class PlayerNameReserved(gameName: String, gameMode: String, id: Int, errorMessage: Option[String])
  extends PreGameMessage
final case class ReserveGameName(gameName: String, gameMode: String) extends PreGameMessage
final case class GameNameReserved(gameName: String, gameMode: String, id: Int, errorMessage: Option[String])
  extends PreGameMessage
final case class GameDoesNotExist(gameName: String) extends PreGameMessage
