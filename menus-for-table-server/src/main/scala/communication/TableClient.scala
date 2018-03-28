package communication

import beforejoining.{OpenTableForm, TableInfo}
import joined.{GameSettings, UserSendingInfoStandard}
import networkcom.tablemessages._
import networkcom.{Client, Message, Peer}

import scala.scalajs.js.timers._

object TableClient extends Client {

  OpenTableForm
  UserSendingInfoStandard

  val address: String = if (scala.scalajs.LinkingInfo.developmentMode) "localhost" else "130.104.85.52"

  val port: Int = 22222

  def messageCallback(client: Client, msg: Message): Unit = {

    msg match {
      case Tables(tables) =>
        TableInfo.updateTables(tables)
      case msg: TableOpened =>
        OpenTableForm.openTableMessage(msg)
      case msg: TableJoined =>
        TableInfo.receivedTableJoined(msg)
      case msg: StandardTableAllInfoMessage =>
        GameSettings.receivedTableInfoMessage(msg)
      case msg: CreateClient =>
        GameSettings.receivedCreateClientMessage(msg)
      case msg: CreateServer =>
        GameSettings.receivedCreateServerMessage(msg)
      case msg: TableDestroyed =>
        GameSettings.tableWasDestroyed(msg)
      case _ =>
        println(s"do not handle this message (yet): $msg")
    }

  }

  def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    println("connected")
  }


  setInterval(1000) {
    sendNormal(
      AskTables()
    )
  }

}
