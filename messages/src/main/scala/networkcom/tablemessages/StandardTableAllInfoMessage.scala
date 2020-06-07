package networkcom.tablemessages

/**
  * @param tableName          name of the table
  * @param tableEnterPassword integer that will be asked when joining the actual game
  * @param gameMode           the mode of the game
  * @param playersInfo        all the information of the players in the game
  */
final case class StandardTableAllInfoMessage(
    tableName: String,
    tableEnterPassword: Int,
    gameMode: String,
    playersInfo: List[StandardTableInfoMessage]
) extends TableServerMessages
