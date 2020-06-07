package networkcom.tablemessages

final case class TableJoined(
    playerName: String,
    tableName: String,
    success: Boolean,
    error: String
) extends TableServerMessages
