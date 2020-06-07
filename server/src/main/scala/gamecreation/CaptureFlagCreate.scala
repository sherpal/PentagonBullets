package gamecreation

import gamelogicserver.CaptureFlagGamePlaying
import gamemode.{CaptureTheFlagMode, GameMode}
import gameserver.GameServer
import networkcom.{Peer, PlayerGameSettingsInfo}

class CaptureFlagCreate(
    val name: String,
    protected val server: GameServer,
    val hostName: String,
    protected val hostPeer: Peer
) extends GameCreation {

  val gameMode: GameMode = CaptureTheFlagMode

  override def makeGamePlaying(password: Int, playerInfo: Vector[PlayerGameSettingsInfo]): CaptureFlagGamePlaying =
    new CaptureFlagGamePlaying(name, password, playerInfo, server)

}
