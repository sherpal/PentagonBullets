package networkcom.tablemessages

/**
  * Sent by PlayerClients to the TableServer to communicate their IP address to the one time server.
  */
final case class Hello(tableName: String) extends TableServerMessages
