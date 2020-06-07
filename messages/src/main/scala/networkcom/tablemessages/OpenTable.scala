package networkcom.tablemessages

/**
  * Requests the server to open a new table.
  */
final case class OpenTable(
    playerName: String,
    name: String,
    gameMode: String,
    password: String
) extends TableServerMessages
