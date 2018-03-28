package networkcom.tablemessages

final case class PlayerPeers(addresses: List[String], ports: List[Int]) extends TableServerMessages
