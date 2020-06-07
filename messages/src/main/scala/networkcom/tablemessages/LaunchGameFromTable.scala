package networkcom.tablemessages

final case class LaunchGameFromTable(tableName: String, success: Boolean, error: String) extends TableServerMessages
