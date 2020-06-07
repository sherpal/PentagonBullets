package server

import gamelogicserver.{CaptureFlagGamePlaying, GameLogicServer, GamePlaying, StandardGamePlaying}
import gamemode.{CaptureTheFlagMode, GameMode, StandardMode}
import networkcom.messages.InGameMessage
import networkcom.tablemessages.PlayerPeers
import networkcom.{Message, Peer, PlayerGameSettingsInfo, Server}
import org.scalajs.dom

final class GameServer(
    gameName: String,
    val port: Int,
    enterPassword: Int,
    playersInfo: List[PlayerGameSettingsInfo],
    gameMode: GameMode
) extends GameLogicServer {

  def closePlayingGame(gameName: String): Unit =
    renderermainprocesscom.Message.sendMessageToMainProcess(
      renderermainprocesscom.CloseMe()
    )

  private val gamePlaying: GamePlaying = gameMode match {
    case StandardMode =>
      new StandardGamePlaying(
        gameName,
        enterPassword,
        playersInfo.toVector,
        this
      )
    case CaptureTheFlagMode =>
      new CaptureFlagGamePlaying(
        gameName,
        enterPassword,
        playersInfo.toVector,
        this
      )
  }

  val address: String = "*"

  def messageCallback(server: Server, peer: Peer, msg: Message): Unit =
    msg match {
      case msg: InGameMessage =>
        gamePlaying.messageCallback(msg, peer)
      case PlayerPeers(addresses, ports) =>
        println(s"pushing connection to ${addresses.zip(ports)}")
        addresses
          .zip(ports)
          .map({ case (a, p) => Peer(a, p) })
          .foreach(server.pushConnection)
      case _ =>
        dom.console.warn(s"received weird message: $msg")
    }

  def clientConnectedCallback(server: Server, peer: Peer, connected: Boolean): Unit =
    if (connected) {
      println(s"a new connection from $peer")
    }

}
