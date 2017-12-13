package gameinfo

import communication.PlayerClient
import custommath.Complex
import gameengine.{Engine, GameState => GameRunner}
import gamegui.ScoreBoard
import gamemode.{GameMode, StandardMode}
import gamestate.ActionSource.PlayerSource
import gamestate.GameState.PlayingState
import gamestate.actions.UpdatePlayerPos
import globalvariables.{DataStorage, StandardModeEOGData}
import graphics.EntityDrawer
import gui.Frame
import io.ControlType.{KeyboardType, MouseType}
import networkcom.PlayerGameSettingsInfo
import org.scalajs.dom
import time.Time

class StandardModeGameHandler(protected val playerName: String,
                              protected val playersInfo: List[PlayerGameSettingsInfo],
                              protected val client: PlayerClient) extends GameHandler {

  val gameMode: GameMode = StandardMode


  def saveGameDataAndLoadScoreBoard(startTime: Long, deadPlayers: List[String]): Unit = {
    DataStorage.storeValue("endOfGameData", StandardModeEOGData(deadPlayers))
    //client.disconnect()
    dom.window.location.href = "./" + GameMode.scoreboards(gameMode)
  }

  /**
   * This state is set up when the player is alive. It manages the game update.
   */
  object Runner extends GameRunner {

    val run: Option[() => Unit] = None

    def draw(): Unit = {
      val gameState = currentGameState
      val gameTime = Time.getTime

      val cameraPos = gameState.players.get(playerId) match {
        case Some(player) =>
          player.pos
        case None =>
          Complex(0,0)
      }

      val timeToUpdate = new java.util.Date().getTime
      EntityDrawer.drawState(cameraPos, gameState, gameTime, playerColors, teamColors, bulletColors)
      computationTimesUpdateDraw.enqueue((new java.util.Date().getTime - timeToUpdate).toInt)
    }

    def keyPressed(key: String, keyCode: Int, isRepeat: Boolean): Unit = {
      //println(key, keyCode)
      pressButton(KeyboardType(), keyCode)

      Frame.keyPressed(key, keyCode, isRepeat)
    }

    def keyReleased(key: String, keyCode: Int): Unit = {
      Frame.keyReleased(key, keyCode)
    }


    def mousePressed(x: Double, y: Double, button: Int): Unit = {
      Frame.clickHandler(x, y, button)

      pressButton(MouseType(), button)
    }

    def mouseMoved(x: Double, y: Double, dx: Double, dy: Double, button: Int): Unit = {
      Frame.mouseMoved(x, y, dx, dy, button)
    }

    def mouseReleased(x: Double, y: Double, button: Int): Unit = {
      Frame.mouseReleased(x, y, button)
    }

    def mouseWheel(dx: Int, dy: Int, dz: Int): Unit = {

      if (dy > 0) {
        focusNextBtn()
      } else if (dy < 0) {
        focusPreviousBtn()
      }

      Frame.wheelMoved(dx, dy)
    }

    def update(dt: Double): Unit = {

      dts.enqueue(dt.toInt)

      actionsThatWhereOnStack = triggeredActions.size

      actionCollector.addActions(triggeredActions)

      otherPlayersPredictions = Nil
      val gameState = currentGameState

      triggeredActions.foreach(GameEvents.fireGameEvent(_, gameState))
      triggeredActions = Nil

      val inUpdate = new java.util.Date().getTime

      val time = Time.getTime
      otherPlayersPredictions = gameState.players.values.filter(_.id != playerId).filter(_.moving).map(
        player => {
          val pos = player.currentPosition(time - player.time)

          UpdatePlayerPos(
            0, time, player.id, pos.re, pos.im, player.direction, player.moving, player.rotation,
            PlayerSource
          )
        }
      )


      ScoreBoard.update(gameState)


      if (gameState.isPlayerAlive(playerId) && gameState.state == PlayingState) {
        val (mouseX, mouseY) = Engine.mousePosition
        val mousePos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
        val playerOpt = playerById(playerId, gameState)
        if (playerOpt.isDefined) {
          val player = playerOpt.get

          val action = movePlayer(gameState, time, dt, mousePos, player)

          unConfirmedActions :+= action
          if (time - lastUpdatePosAction.time > 100 || action.dir != lastUpdatePosAction.dir ||
            action.moving != lastUpdatePosAction.moving) {
            buffer :+= action
            lastUpdatePosAction = action
          }

          EntityDrawer.camera.worldCenter = Complex(action.x, action.y)

        }

      }
      inUpdateFunction.enqueue((new java.util.Date().getTime - inUpdate).toInt)

      val inUpdateHandler = new java.util.Date().getTime
      Frame.updateHandler(dt)
      inUpdateHandlers.enqueue((new java.util.Date().getTime - inUpdateHandler).toInt)

      {
        val debug = new java.util.Date().getTime

        computationTimes.enqueue(Engine.computationTime.toInt)
        lastComputationTime = computationTimes.sum / computationTimes.size

        if (computationTimes.lengthCompare(20) > 0) computationTimes.dequeue()

        renderTimes.enqueue(Engine.renderTime.toInt)
        val renderTime = renderTimes.sum / renderTimes.size

        if (renderTimes.lengthCompare(20) > 0) renderTimes.dequeue()

        val processActionTime = processActions.sum / processActions.size
        if (processActions.lengthCompare(20) > 0) processActions.dequeue()

        val updateDrawTime = computationTimesUpdateDraw.sum / computationTimesUpdateDraw.size
        if (computationTimesUpdateDraw.lengthCompare(20) > 0)
          computationTimesUpdateDraw.dequeue()

        val dtTime = dts.sum / dts.size
        if (dts.lengthCompare(20) > 0) dts.dequeue()

        val inUpdate = inUpdateFunction.sum / inUpdateFunction.size
        if (inUpdateFunction.lengthCompare(20) > 0) inUpdateFunction.dequeue()

        val inUpdateHandler = inUpdateHandlers.sum / inUpdateHandlers.size
        if (inUpdateHandlers.lengthCompare(20) > 0) inUpdateHandlers.dequeue()

        debugTime.enqueue((new java.util.Date().getTime - debug).toInt)
        val dT = debugTime.sum / debugTime.size
        if (debugTime.lengthCompare(20) > 0) debugTime.dequeue()

        val info = List(
          lastComputationTime, updateDrawTime, renderTime, processActionTime, inUpdate, inUpdateHandler,
          dT, dtTime
        ).mkString(" ms; ")

        if (info != computationTimeFS.text) {
          computationTimeFS.setText(info)
        }

        maxActionsOnStack = math.max(maxActionsOnStack, actionsThatWhereOnStack)

        actionsOnStackFS.setText(s"AoS: $maxActionsOnStack")
      }

    }

  }
}
