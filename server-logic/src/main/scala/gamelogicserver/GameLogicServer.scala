package gamelogicserver

import networkcom.Server

trait GameLogicServer extends Server {

  def closePlayingGame(gameName: String): Unit

}
