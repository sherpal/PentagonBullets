package renderer

import debug.DebugPackage
import exceptions.NotAnActionMessage
import gamemessages.MessageMaker
import gamereconstruction.ReplayGameMode
import gamestate.{ActionCollector, GameState}
import networkcom.messages.ActionMessage
import networkcom.{Message => ActionMessageObject}
import org.scalajs.dom
import pixigraphics.{PIXILoader, PIXIResource}
import renderermainprocesscom.StoreGameInfo.PlayerInfo
import renderermainprocesscom._
import sharednodejsapis.{IPCMainEvent, IPCRenderer}

import scala.scalajs.js

object Replay {

  var replayGameMode: ReplayGameMode = _

  def main(args: Array[String]): Unit = {

    new DebugPackage("../gameplaying/replay.html")

    var _toReceive = 0
    var _received = 0
    var _receivedActionBytes: List[Vector[Byte]] = List()

    var actionCollector: ActionCollector = null
    var playersInfo: Vector[PlayerInfo] = null
    var teamLeaders: Vector[Long] = null
    var gameName: String = null

    def allMessageReceived(): Unit = {
      println("end of receiving actions")

      val loader: PIXILoader = new PIXILoader

      // load the font.
      loader
        .add("../../assets/font/quicksand_0.png")
        .add("../../assets/font/quicksand_1.png")
        .add("../../assets/font/quicksand.fnt")
        .add("../../assets/font/quicksand_bold_1.png")
        .add("../../assets/font/quicksand_bold.fnt")

      loader.load((_: PIXILoader, _: js.Dictionary[PIXIResource]) => {
        try {
          val t = new java.util.Date().getTime
          actionCollector.addActions(_receivedActionBytes
            .map(_.toArray)
            .map(ActionMessageObject.decode)
            .map({
              case action: ActionMessage => action
              case message => throw new NotAnActionMessage(s"Message was of class ${message.getClass}")
            })
            .map(MessageMaker.messageToAction)
            .sortBy(_.time))

          println(s"It took ${(new java.util.Date().getTime - t) / 1000.0}s to reconstruct the GameStates.")

          replayGameMode = new ReplayGameMode(gameName, actionCollector, playersInfo, teamLeaders)
        } catch {
          case e: NotAnActionMessage => dom.console.error(e.msg)
          case e: Throwable => e.printStackTrace()
        }

        Message.sendMessageToMainProcess(ReadyToShow(0))
      })

    }

    val onMessage: js.Function2[IPCMainEvent, Any, Unit] = (_: IPCMainEvent, msg: Any) => {
      Message.decode(msg.asInstanceOf[scala.scalajs.js.Array[Byte]]) match {
        case GiveGameInfoBack.GeneralGameInfo(
        gName, startingGameTime, pInfo, numberOfActions) =>
          _toReceive = numberOfActions

          actionCollector = new ActionCollector(
            GameState.originalState.timeUpdate(startingGameTime),
            1000, Long.MaxValue
          )

          playersInfo = pInfo.info
          teamLeaders = pInfo.teamLeaders
          gameName = gName

        case GiveGameInfoBack.SendActionGroup(actions) =>
          _received += actions.length
          _receivedActionBytes = actions.reverse ++ _receivedActionBytes
          println(s"received: ${_received}")
          if (_received == _toReceive) {
            allMessageReceived()
          }

        case _ =>
      }
    }

    IPCRenderer.on("main-renderer-message", onMessage)

    Message.sendMessageToMainProcess(StoreGameInfo.GiveMeGameInfo())


  }

}
