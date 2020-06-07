package networkcom.tablemessages

final case class AskTableInfo(tableName: String, playerAsking: String) extends TableServerMessages
