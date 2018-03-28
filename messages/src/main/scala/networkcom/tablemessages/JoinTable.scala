package networkcom.tablemessages

final case class JoinTable(playerName: String, tableName: String, password: String) extends TableServerMessages
