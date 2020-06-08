package renderer

import entities.Entity
import globalvariables.{DataStorage, OneTimeServerGameData}
import networkcom.{Peer, PlayerGameSettingsInfo}
import networkcom.tablemessages.HolePunching
import server.GameServer
import sharednodejsapis.portfinder.PortFinder

object Renderer {

  var server: GameServer = _

  def main(args: Array[String]): Unit = {
    /**
      * First finding an available port.
      */
    //PortFinder.getPort((_, port) => {
      val port = 22223

      println(s"Port: $port")

      DataStorage.retrieveGlobalValue("gameData") match {
        case OneTimeServerGameData(
            gameName,
            gameMode,
            enterPassword,
            tableServerAddress,
            tableServerPort,
            playerData
            ) =>
          server = new GameServer(
            gameName,
            port,
            enterPassword,
            playerData.map(data => {
              PlayerGameSettingsInfo.fromInfo(
                data.playerName,
                ready = true,
                data.team,
                data.id,
                data.abilities,
                (data.color(0), data.color(1), data.color(2))
              )
            }),
            gameMode
          )

          /**
            * Removing ids from available ids.
            */
          playerData.foreach(_ => Entity.newId())

          val peer = Peer(tableServerAddress, tableServerPort)

          server.activate()
          server.makeConnection(peer)
          server.sendOrderedReliable(
            HolePunching(gameName),
            peer
          )

        case _ =>
          println("Game data were not saved correctly.")
      }
    //})
  }
}
