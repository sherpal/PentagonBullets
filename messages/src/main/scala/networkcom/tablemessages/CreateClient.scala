package networkcom.tablemessages

import networkcom.Peer

/**
  * Sends a message to the players that they can launch the client, and connect to the server connected at peer Peer.
  */
final case class CreateClient(peer: Peer) extends TableServerMessages
