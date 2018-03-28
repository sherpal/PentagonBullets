package networkcom.tablemessages

final case class StandardTableInfoMessage(
                                           tableName: String,
                                           playerName: String,
                                           playerId: Long,
                                           team: Int,
                                           abilities: List[Int],
                                           ready: Boolean,
                                           color: Vector[Double]
                                         ) extends PlayerInfoMessage
