package networkcom.messages

/**
  * These messages are sent during the game.
  * Either the server tells the clients something happen, or the clients communicate their actions.
  */
trait InGameMessage extends networkcom.Message {
  val gameName: String
}
final case class PlayerConnecting(gameName: String, playerName: String, password: Int) extends InGameMessage
final case class StillWaitingForPlayers(gameName: String, n: Int) extends InGameMessage
final case class GameStarts(gameName: String) extends InGameMessage
final case class ClosingGame(gameName: String, msg: String) extends InGameMessage
final case class GameStartsIn(gameName: String, time: Long) extends InGameMessage

final case class GuessClockTime(gameName: String, time: Long) extends InGameMessage
final case class AnswerGuessClockTime(gameName: String, guessed: Long, actual: Long) extends InGameMessage

final case class ActionsMessage(gameName: String, actions: List[ActionMessage]) extends InGameMessage
final case class ActionDenied(gameName: String, action: ActionMessage) extends InGameMessage
final case class ActionsDenied(gameName: String, actions: List[ActionMessage]) extends InGameMessage
final case class DeleteActions(gameName: String, fromTime: Long, actionIds: List[Long]) extends InGameMessage
