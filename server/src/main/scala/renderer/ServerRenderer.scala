package renderer

import gameserver.GameServer
import globalvariables.VariableStorage

object ServerRenderer {
  def main(args: Array[String]): Unit = {

    val port = VariableStorage.retrieveGlobalValue("serverPort").asInstanceOf[String].toInt

    val server = new GameServer("*", port)
    server.activate()

  }
}
