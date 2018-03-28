package networkcom.tablemessages

final case class Table(
                      name: String,
                      gameMode: String,
                      playerNames: List[String],
                      password: String
                      ) extends TableServerMessages
