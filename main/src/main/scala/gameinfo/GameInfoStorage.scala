package gameinfo

import renderermainprocesscom.{Message, StoreGameInfo}
import renderermainprocesscom.StoreGameInfo._
import sharednodejsapis.WebContents

import scala.scalajs.js.timers.setTimeout

/**
  * Takes care of information sent to the MainProcess for keeping track of the game information.
  * When a Player wants to use the Replay mode, it can ask the MainProcess and it will give back everything that it
  * stored.
  */
object GameInfoStorage {

  /**
    * Manages everything when messages are received from the client.
    */
  def messageHandler(message: StoreGameInfo, webContents: WebContents): Unit = message match {
    case StoreAction(compressedAction) =>
      addActionInfo(compressedAction)
    case PlayersInfo(info, teamLeaders) =>
      _playersInfo = PlayersInfo(info, teamLeaders)
    case NewGame(gameName, initialGameStateTime) =>
      println(gameName, initialGameStateTime)
      resetInfo()
      _gameName             = gameName
      _initialGameStateTime = initialGameStateTime
    case GiveMeGameInfo() =>
      sendGameInfo(webContents)
  }

  private var _gameName: String                = ""
  private var _initialGameStateTime: Long      = 0
  private var _actionsInfo: List[Vector[Byte]] = Nil
  private var _playersInfo: PlayersInfo        = PlayersInfo(Vector(), Vector())

  /**
    * Adds the actionInfo to the list.
    * The order in which action are stored does not matter, since the BrowserWindow will sort the actions back with
    * respect to time.
    */
  private def addActionInfo(actionInfo: Vector[Byte]): Unit = {
    _actionsInfo = actionInfo +: _actionsInfo

    if (_actionsInfo.length % 100 == 0) println(_actionsInfo.length)
  }

  private def resetInfo(): Unit = {
    _gameName    = ""
    _actionsInfo = Nil
  }

  /**
    * Manages everything when a client asks for GameInfo.
    */
  def sendGameInfo(webContents: WebContents): Unit = {
    import renderermainprocesscom.GiveGameInfoBack._

    Message.sendMessageToWebContents(
      webContents,
      GeneralGameInfo(
        _gameName,
        _initialGameStateTime,
        _playersInfo,
        _actionsInfo.length
      )
    )

    val groupedActions = _actionsInfo.grouped(10000).toList

    groupedActions
      .zip(1 to groupedActions.size)
      .foreach({
        case (group, idx) =>
          setTimeout(idx * 500) {
            Message.sendMessageToWebContents(
              webContents,
              SendActionGroup(group)
            )
          }
      })

  }

}
