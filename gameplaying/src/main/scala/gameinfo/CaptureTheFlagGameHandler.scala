package gameinfo

import abilities.Teleportation
import communication.PlayerClient
import custommath.Complex
import entities.{Player, TeamFlag}
import entitiescollections.CaptureTheFlagInfo
import gameengine.{Engine, GameState => GameRunner}
import gamegui._
import gamemode.{CaptureTheFlagMode, GameMode}
import gamestate.ActionSource.PlayerSource
import gamestate.GameState.{PlayingState, PreBegin}
import gamestate.GameAction
import gamestate.actions._
import globalvariables.{CaptureTheFlagModeEOGData, DataStorage}
import graphics.{EntityDrawer, GameAnimation}
import graphics.pixitexturemakers.TeamFlagTextureMaker
import gui.{BottomLeft, Center, Frame}
import io.ControlType.{KeyboardType, MouseType}
import networkcom.PlayerGameSettingsInfo
import org.scalajs.dom
import time.Time

import scala.scalajs.js.timers.setInterval


class CaptureTheFlagGameHandler(protected val playerName: String,
                                protected val playersInfo: List[PlayerGameSettingsInfo],
                                protected val client: PlayerClient) extends GameHandler {

  val gameMode: GameMode = CaptureTheFlagMode

  def saveGameDataAndLoadScoreBoard(startTime: Long, deadPlayers: List[String]): Unit = {
    DataStorage.storeValue("endOfGameData", CaptureTheFlagModeEOGData(scores))
    dom.window.location.href = "./" + GameMode.scoreboards(gameMode)
  }


  TeamFlagTextureMaker

  val captureTheFlagInto: CaptureTheFlagInfo = new CaptureTheFlagInfo(
    playersInfo.map(info => CaptureTheFlagInfo.PlayerInfo(info.playerName, info.abilities, info.team, info.id))
  )

  private def scores: Map[Int, Int] = TeamFlag.scores(currentGameState)

  lazy val playerHealthBars: Map[Long, PlayerHealthBar] = playersInfo
    .map(_.id)
    .map(id => id -> new PlayerHealthBar(id, this))
    .toMap

  object Runner extends GameRunner {

    val run: Option[() => Unit] = None

    def draw(): Unit = {
      val gameState = currentGameState
      val gameTime = Time.getTime

      val cameraPos = gameState.players.get(playerId) match {
        case Some(player) =>
          player.pos
        case None =>
          gameState.deadPlayers.get(playerId) match {
            case Some(player) =>
              captureTheFlagInto.popPositionsByPlayerId(player.id)
            case None =>
              Complex(0, 0)
          }
      }

      GameAnimation.animate(gameState, gameTime, EntityDrawer.camera)

      val timeToUpdate = new java.util.Date().getTime
      EntityDrawer.drawState(cameraPos, gameState, gameTime, playerColors, teamColors, bulletColors)
      computationTimesUpdateDraw.enqueue((new java.util.Date().getTime - timeToUpdate).toInt)
    }

    private val resurrectionCountdown: Countdown = new Countdown(Center, 20, 20)
    resurrectionCountdown.registerEvent(GameEvents.OnPlayerDead)((action, _) => {
      if (action.playerId == playerId) {
        resurrectionCountdown.startClock(CaptureTheFlagInfo.resurrectionTime)
      }
    })



    def keyPressed(key: String, keyCode: Int, isRepeat: Boolean): Unit = {
      //useAbility(key, gameState)
      //println(key, keyCode)



//      bindings.abilities.indexOf((KeyboardType(), keyCode)) match {
//        case -1 =>
//        case idx => useAbility(idx, gameState)
//      }
//      KeyBindingsLoader.defaultBindings2.abilities.indexOf(keyCode) match {
//        case -1 =>
//        case idx => useAbility(idx, gameState)
//      }
      pressButton(KeyboardType(), keyCode)

      Frame.keyPressed(key, keyCode, isRepeat)
    }

    def keyReleased(key: String, keyCode: Int): Unit = {

      Frame.keyReleased(key, keyCode)
    }

    def mousePressed(x: Double, y: Double, button: Int): Unit = {
      Frame.clickHandler(x, y, button)

      pressButton(MouseType(), button)

//      if (button == 0) {
//        val gameState = currentGameState
//        if (gameState.isPlayerAlive(playerId) && gameState.state == PlayingState) {
//          val (mouseX, mouseY) = Engine.mousePosition
//          val mousePos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
//          val playerOpt = playerById(playerId, gameState)
//          if (playerOpt.isDefined) {
//            val player = playerOpt.get
//
//            val rotation = (mousePos - player.pos).arg
//            val startingPos = player.pos + Player.radius * Complex.rotation(rotation)
//
//            client.sendAction(NewBullet(
//              Entity.newId(), playerId, teamsByPlayerId(playerId),
//              startingPos, Bullet.defaultRadius, rotation, Bullet.speed, Time.getTime, 0,
//              PlayerSource
//            ))
//
//          }
//        }
//      } else {
//        val gameState = currentGameState
//        if (gameState.isPlayerAlive(playerId) && gameState.state == PlayingState) {
//          val playerOpt = playerById(playerId, gameState)
//          if (playerOpt.isDefined) {
//            useAbility(
//              playerOpt.get.allowedAbilities.distinct.indexOf(abilityButtons(focusBtnIndex).abilityId),
//              gameState
//            )
//          }
//        }
//      }
    }

    def mouseMoved(x: Double, y: Double, dx: Double, dy: Double, button: Int): Unit = {
      Frame.mouseMoved(x, y, dx, dy, button)
    }

    def mouseReleased(x: Double, y: Double, button: Int): Unit = {
      Frame.mouseReleased(x, y, button)
    }

    def mouseWheel(dx: Int, dy: Int, dz: Int): Unit = {
      Frame.wheelMoved(dx, dy)
    }

    def update(dt: Double): Unit = {


      actionCollector.addActions(triggeredActions)

      otherPlayersPredictions = Nil
      val gameState = currentGameState

      triggeredActions.foreach(GameEvents.fireGameEvent(_, gameState))
      triggeredActions = Nil

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
      } else {
        EntityDrawer.camera.worldCenter = captureTheFlagInto.popPositionsByPlayerId(playerId)
      }


      Frame.updateHandler(dt)
    }

  }


  override def addAction(action: GameAction, needUpdate: Boolean = true): Unit = {
    triggeredActions :+= action

    if (isPredictableAction(action)) {
      unConfirmedActions = unConfirmedActions.dropWhile(_.time < action.time)
    } else action match {
      case UseAbilityAction(_, _, ability, _, _)
        if ability.isInstanceOf[Teleportation] && ability.casterId == playerId =>
        unConfirmedActions = Nil
      case a: PlayerDead =>
        if (a.playerId == playerId) {
          unConfirmedActions = Nil
          buffer = Nil
        }
        otherPlayersPredictions = otherPlayersPredictions.filter(_.playerId != a.playerId)
      case _: GameEndedAction =>
        super.addAction(action)


      case _: GameBegins =>
        playerHealthBars.values.foreach(HealthBar.addBar)
        currentGameState.players.values.foreach(player => playerNamesById += (player.id -> player.name))

        CaptureTheFlagScoreBoard.colorMap = teamColors
        CaptureTheFlagScoreBoard.setScores(TeamFlag.scores(currentGameState))

        ScoreBoard.clearAllPoints()
        ScoreBoard.setPoint(BottomLeft)

        gameClock.startClock()

        PlayerNameFS.hideFontStrings()

        pendingActionHandler = Some(setInterval(1000 / 15) {
          sendPendingActions()
        })
      case a: NewPlayer if currentGameState.state == PreBegin =>
        if (a.player.id == playerId) {
          lastUpdatePosAction =
            UpdatePlayerPos(0, a.time, playerId, a.player.xPos, a.player.yPos, 0, moving = false, 0, PlayerSource)
          EntityDrawer.camera.worldCenter = a.player.pos
          PlayerNameFS.placeFontStrings()
          if (a.player.allowedAbilities.nonEmpty) {
            abilityButtons = a.player.allowedAbilities.tail.foldLeft(
              List(new AbilityButton(a.player.allowedAbilities.head, playerId))
            )({ case (buts, id) =>
              new AbilityButton(id, playerId, Some(buts.head)) +: buts
            })
            changeFocusedBtn(0)
          }
        }
        PlayerNameFS.newName(a.player.name, a.player.pos + Complex(0, Player.radius + 2))
      case _ =>
    }

  }


}
