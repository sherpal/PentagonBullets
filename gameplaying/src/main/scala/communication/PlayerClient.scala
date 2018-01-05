package communication

import networkcom._
import networkcom.messages._
import gameengine.Engine
import gamegui.Countdown
import gameinfo.{CaptureTheFlagGameHandler, GameHandler, PreGameRunner, StandardModeGameHandler}
import gamemode.{CaptureTheFlagMode, GameMode, StandardMode}
import gamestate.GameAction
import gamestate.GameState.GameEnded
import graphics.EntityDrawer
import graphics.gameanimations.LaserGunSight
import gui.Center
import org.scalajs.dom
import org.scalajs.dom.html
import pixigraphics.{PIXILoader, PIXIResource}
import time.Time

import scala.scalajs.js


/**
 * The PlayerClient communicates with the server, and creates the GameHandler that will manage the game.
 *
 * This class is a priori good as it is, and won't change in the future, except for adding all the different game modes
 * that will arrive.
 */
class PlayerClient(playerName: String,
                   val gameName: String,
                   val address: String,
                   val port: Int,
                   val password: Int,
                   playersInfo: List[PlayerGameSettingsInfo],
                   val gameMode: GameMode) extends Client {

  Engine.changeGameState(PreGameRunner)
  Engine.startGameLoop()


  PlayerClient._playerClient = this

  lazy val gameHandler: GameHandler = gameMode match {
    case StandardMode => new StandardModeGameHandler(playerName, playersInfo, this)
    case CaptureTheFlagMode => new CaptureTheFlagGameHandler(playerName, playersInfo, this)
    case _ => throw new NotImplementedError(s"$gameMode is not yet implemented.")
  }

  connect()

  def serverTime: Long = Time.getTime

  def guessClock(): Unit = {
    sendReliable(GuessClockTime(gameName, serverTime + meanLatency))
  }


  def messageCallback(client: Client, msg: Message): Unit = {
    msg match {
      case ActionsMessage(_, actions) =>
        gameHandler.addActions(actions.map(MessageMaker.messageToAction))

      case action: ActionMessage =>
        gameHandler.addAction(MessageMaker.messageToAction(action))

      case ActionsDenied(_, actions) =>
        gameHandler.actionsDenied(actions.map(MessageMaker.messageToAction))

      case ActionDenied(_, action) =>
        gameHandler.actionDenied(MessageMaker.messageToAction(action))

      case DeleteActions(_, oldestTime, actionIds) =>
        gameHandler.deleteActions(oldestTime, actionIds)

      case GameStartsIn(_, time) =>
        new Countdown(Center, 10, 32)
          .startClock(time)

      case GameStarts(_) =>
        gameStart()

      case StillWaitingForPlayers(_, n) =>
        println(s"still waiting for $n players")

      case AnswerGuessClockTime(_, guessed, actual) =>
        println(
          s"I guessed it would be $guessed. It was actually $actual. Absolute error: ${math.abs(guessed - actual)}."
        )

      case ClosingGame(_, message) =>

        if (message == "gameEndedNormally") {
          disconnect()
          dom.window.location.href = "../../gamemenus/mainscreen/index.html" // TODO: change this
        } else {
          dom.window.alert(message)
          dom.window.location.href = "../../gamemenus/mainscreen/index.html"
        }

      case _ =>
        println(s"Unknown message: $msg")
    }
  }

  def synchronize(): Unit =
    computeLinkTime(sampleNumber = 50, endCallback = (delta: Long) => {
      Time.setDeltaTime(delta)
    })


  private var deltaTimeReady: Boolean = false
  private var resourceLoaderReady: Boolean = false
  def readyToConnect(): Unit = {
    if (deltaTimeReady && resourceLoaderReady) {
      sendReliable(PlayerConnecting(gameName, playerName, password))
    }

  }

  private val loader: PIXILoader = new PIXILoader

  // load the font.
  loader
    .add("../../assets/font/quicksand_0.png")
    .add("../../assets/font/quicksand_1.png")
    .add("../../assets/font/quicksand.fnt")
    .add("../../assets/font/quicksand_bold_1.png")
    .add("../../assets/font/quicksand_bold.fnt")

  loader.load((_: PIXILoader, _: js.Dictionary[PIXIResource]) => {
    resourceLoaderReady = true

    readyToConnect()
  })



  override def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit = {
    if (connected) {
      // we first try to have the server time by computing it.
      computeLinkTime(sampleNumber = 50, endCallback = (delta: Long) => {
        Time.setDeltaTime(delta)
        activatePing()
        deltaTimeReady = true

        gameHandler

        readyToConnect()
      })
    } else if (gameHandler.currentGameState.state != GameEnded) {
      dom.window.alert("You have been disconnected from the server.")
      dom.window.location.href = "../../gamemenus/mainscreen/index.html"
    }
  }

  def setMaxCanvasDimensions(): Unit = {
    val gameInterface: html.Div = dom.document.getElementById("gameInterface").asInstanceOf[html.Div]
    gameInterface.style.width = dom.window.innerWidth.toInt + "px"
    gameInterface.style.height = dom.window.innerHeight.toInt + "px"

    val windowWidth: Int = dom.window.innerWidth.toInt - 7
    val windowHeight: Int = dom.window.innerHeight.toInt - 7

    val canvasHeight = math.min(windowHeight, windowWidth * EntityDrawer.cameraWidthToHeightRatio).toInt
    val canvasWidth = (canvasHeight / EntityDrawer.cameraWidthToHeightRatio).toInt

    Engine.graphics.resize(canvasWidth, canvasHeight)
  }

  dom.window.addEventListener[dom.UIEvent]("resize", (_: dom.UIEvent) => {
    setMaxCanvasDimensions()
  })

  setMaxCanvasDimensions()


  private def gameStart(): Unit = {

    PreGameRunner.removePentagon()
    Engine.changeGameState(gameHandler.Runner)
    Engine.startGameLoop()

    new LaserGunSight(
      playersInfo.find(_.playerName == playerName).get.id,
      EntityDrawer.laserLauncherAnimationStage,
      gameHandler.playerColors(playersInfo.find(_.playerName == playerName).get.id)
    )

  }


  def sendAction(action: GameAction): Unit = {
    sendNormal(MessageMaker.toMessage(gameName, action))
  }

  def sendActions(actions: List[GameAction]): Unit = {
    sendNormal(ActionsMessage(gameName, actions.map(MessageMaker.toMessage(gameName, _))))
  }



}


object PlayerClient {

  /**
   * Keeping a global reference of the player client created for the game
   */
  private var _playerClient: PlayerClient = _

  def playerClient: PlayerClient = _playerClient

}