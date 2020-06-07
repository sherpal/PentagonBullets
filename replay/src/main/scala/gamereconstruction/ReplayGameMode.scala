package gamereconstruction

import custommath.Complex
import entities.GameArea
import gameengine.{Engine, GameState => GameRunner}
import gamegui.{GameClock, HealthBar, PlayerHealthBar, ScoreBoard}
import gamestate.ActionSource.PlayerSource
import gamestate.actions.UpdatePlayerPos
import gamestate.{ActionCollector, GameAction, GameState}
import graphics.EntityDrawer
import gui.Frame
import org.scalajs.dom
import org.scalajs.dom.html
import renderermainprocesscom.StoreGameInfo.PlayerInfo
import replayui.PointOfViewSelector

import scala.language.implicitConversions

class ReplayGameMode(
    gameName: String,
    actionCollector: ActionCollector,
    playersInfo: Vector[PlayerInfo],
    teamLeaders: Vector[Long]
) {

  Engine.graphics.resize(EntityDrawer.worldWidth / 3 * 2, EntityDrawer.worldHeight / 3 * 2)

  val canvas: html.Canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]

  val playButton: html.Button  = dom.document.getElementById("playButton").asInstanceOf[html.Button]
  val pauseButton: html.Button = dom.document.getElementById("pauseButton").asInstanceOf[html.Button]

  val timeSlider: html.Input = dom.document.getElementById("timeSlider").asInstanceOf[html.Input]
  timeSlider.style.width = canvas.width.toString + "px"

  ("spectator" +: playersInfo.map(_.name)).map(PointOfViewSelector(_, this))

  import ReplayGameMode.VectorToColor

  val lastGameState: GameState = actionCollector.gameStateUpTo(new java.util.Date().getTime)

  val lastTime: Long = lastGameState.time

  val firstGameState: GameState = actionCollector.originalGameState

  val firstTime: Long = firstGameState.time + 4000

  val playerColors: Map[Long, (Double, Double, Double)] =
    playersInfo.map({ case PlayerInfo(id, _, color, _) => id -> color.toColor }).toMap

  val colorByPlayerName: Map[String, (Double, Double, Double)] =
    playersInfo.map({ case PlayerInfo(id, name, _, _) => name -> playerColors(id) }).toMap

  val teamColors: Map[Int, (Double, Double, Double)] =
    teamLeaders.map(id => playersInfo.find(_.id == id).get.team -> playerColors(id)).toMap

  val bulletColors: Map[Long, (Double, Double, Double)] =
    playersInfo.map({ case PlayerInfo(id, _, _, team) => id -> teamColors(team) }).toMap

  /** Creating the ScoreBoard */
  ScoreBoard
  ScoreBoard.colorMap = colorByPlayerName
  playersInfo.foreach({
    case PlayerInfo(id, name, color, _) =>
      ScoreBoard.addPlayerLife(name, id, color.toColor)
  })

  /** The Game Clock */
  val gameClock: GameClock = new GameClock

  /**
    * The pointOfView determines from what perspective the game is seen during the replay.
    *
    * pointOfView can either be "spectator" (default), in which case the entire game is rendered, or the name of one of
    * the players, and in that case the game is rendered through their eyes.
    */
  private var pointOfView: String = "spectator"

  def setPointOfView(name: String): Unit =
    pointOfView = name

  /**
    * The time being rendered on screen.
    *
    * It can evolve in many ways.
    */
  private var currentTime: Long = firstTime

  timeSlider.min   = firstTime.toString
  timeSlider.max   = lastTime.toString
  timeSlider.value = currentTime.toString

  private var slidingTime: Boolean = false

  timeSlider.onmousedown = (_: dom.MouseEvent) => {
    slidingTime = true
    _playing    = false
  }

  timeSlider.onmouseup = (_: dom.MouseEvent) => {
    slidingTime = false
  }

  timeSlider.onchange = (_: dom.Event) => {
    currentTime = timeSlider.value.toLong
  }

  /**
    * Whether the replay mode is in motion as time goes.
    */
  private var _playing: Boolean = true

  private var _zoom: Double = 1.0

  def zoomIn(): Unit =
    _zoom *= 1.1

  def zoomOut(): Unit =
    _zoom = math.max(1.0, _zoom / 1.1)

  /**
    * Remembers the center of the view in spectator mode.
    */
  private var _center: Complex = Complex(0, 0)

  /**
    * Keeps tracks whether the player is dragging the spectator mode.
    */
  private var _dragging: Boolean = false

  def gameState(time: Long): GameState = actionCollector.gameStateUpTo(time)

  private var currentGameState: GameState = firstGameState

  val playerBars: Map[Long, PlayerHealthBar] =
    playersInfo
      .map(
        { case PlayerInfo(id, _, _, _) => id -> new PlayerHealthBar(id, () => this.currentGameState.players.get(id)) }
      )
      .toMap

  playerBars.values.foreach(HealthBar.addBar)

  private def reset(): Unit = {
    currentTime = firstTime
    ScoreBoard.reset(currentTime)
  }

  private def setWorldDimensions(width: Double, height: Double): Unit = {
    EntityDrawer.camera.worldWidth  = width / _zoom
    EntityDrawer.camera.worldHeight = height / _zoom
  }

  private def worldMousePosition: Complex = {
    val (x, y) = Engine.mousePosition

    EntityDrawer.camera.mousePosToWorld(Complex(x, y))
  }

  object ReplayGameRunner extends GameRunner {

    val run: Option[() => Unit] = None

    def draw(): Unit = {

      val cameraPos = if (pointOfView == "spectator") {
        val (w, h) = Engine.graphics.dimensions

        setWorldDimensions(
          GameArea.sizeFromNbrPlayers(playersInfo.length) * math.max(1, w / h.toDouble),
          GameArea.sizeFromNbrPlayers(playersInfo.length) * math.max(1, h.toDouble / w)
        )

        _center
      } else {
        setWorldDimensions(EntityDrawer.worldWidth, EntityDrawer.worldHeight)

        currentGameState.players.values.find(_.name == pointOfView) match {
          case Some(player) => player.pos
          case None         => _center
        }
      }

      val gameTime = if (currentTime > lastGameState.time) lastGameState.time else currentTime

      EntityDrawer.drawState(cameraPos, currentGameState, gameTime, playerColors, teamColors, bulletColors)

    }

    def keyPressed(key: String, keyCode: Int, isRepeat: Boolean): Unit =
      Frame.keyPressed(key, keyCode, isRepeat)

    def keyReleased(key: String, keyCode: Int): Unit =
      Frame.keyReleased(key, keyCode)

    def mousePressed(x: Double, y: Double, button: Int): Unit = {
      _dragging = true

      Frame.clickHandler(x, y, button)
    }

    def mouseMoved(x: Double, y: Double, dx: Double, dy: Double, button: Int): Unit = {
      if (_dragging && _zoom > 1.0) {
        _center += Complex(-dx, -dy)
      }

      Frame.mouseMoved(x, y, dx, dy, button)
    }

    def mouseReleased(x: Double, y: Double, button: Int): Unit = {
      _dragging = false

      Frame.mouseReleased(x, y, button)
    }

    def mouseWheel(dx: Int, dy: Int, dz: Int): Unit = {

      if (dy > 0) {
        zoomOut()
        if (_zoom == 1) {
          _center = Complex(0, 0)
        }
      } else if (dy < 0) {
        zoomIn()
        _center = worldMousePosition
      }

      Frame.wheelMoved(dx, dy)
    }

    def update(dt: Double): Unit = {

      if (_playing) {
        currentTime += dt.toLong
        if (currentTime > lastGameState.time + 2000) {
          reset()
        }
      }

      if (slidingTime) {
        currentTime = timeSlider.value.toLong
      } else {
        timeSlider.value = currentTime.toString
      }

      val gameTime = if (currentTime > lastGameState.time) lastGameState.time else currentTime

      currentGameState = gameState(gameTime)

      // adding player positions
      currentGameState = currentGameState.apply(
        currentGameState.players.values.toList.map(player => {
          val pos = player.currentPosition(gameTime - player.time)
          UpdatePlayerPos(
            GameAction.newId(),
            gameTime,
            player.id,
            pos.re,
            pos.im,
            player.direction,
            player.moving,
            player.rotation,
            PlayerSource
          )
        })
      )

      HealthBar.updateBars()
      ScoreBoard.update(currentGameState)

      gameClock.setTime(if (currentGameState.started) (gameTime - currentGameState.startTime.get) / 1000.0 else 0.0)

      Frame.updateHandler(dt)
    }

  }

  Engine.changeGameState(ReplayGameRunner)
  Engine.startGameLoop()

  playButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => _playing  = true)
  pauseButton.addEventListener[dom.MouseEvent]("click", (_: dom.MouseEvent) => _playing = false)

}

object ReplayGameMode {

  private implicit class VectorToColor(color: Vector[Double]) {

    def toColor: (Double, Double, Double) = (color(0), color(1), color(2))

  }

}
