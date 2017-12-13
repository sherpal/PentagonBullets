package gamecreation

import gamelogicserver.StandardGamePlaying
import gamemode.{GameMode, StandardMode}
import gameserver.GameServer
import networkcom.{Peer, PlayerGameSettingsInfo}

class StandardGameCreate(val name: String, protected val server: GameServer, val hostName: String,
                         protected val hostPeer: Peer) extends GameCreation {

  val gameMode: GameMode = StandardMode

  def makeGamePlaying(password: Int, playerInfo: Vector[PlayerGameSettingsInfo]): StandardGamePlaying =
    new StandardGamePlaying(name, password, playerInfo, server)

}
