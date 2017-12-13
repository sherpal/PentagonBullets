package gameinfo

import abilities._
import communication.PlayerClient
import custommath.Complex
import entities.{Bullet, Entity, GameArea, Player}
import entitiescollections.PlayerTeam
import gameengine.{Engine, GameState => GameRunner}
import gamegui._
import gamemode.GameMode
import gamestate.ActionSource.PlayerSource
import gamestate.GameState.PlayingState
import gamestate.actions._
import gamestate.{ActionCollector, GameAction, GameState}
import graphics.{Color, EntityDrawer}
import gui._
import io.{ControlBindings, ControlType, KeyBindingsLoader}
import networkcom.PlayerGameSettingsInfo
import networkcom.messages._
import networkcom.messages.{Point => NetworkPoint}
import org.scalajs.dom
import time.Time

import scala.collection.mutable
import scala.language.implicitConversions
import scala.scalajs.js.timers.{SetIntervalHandle, clearInterval, setInterval}

/**
 * The GameHandler receives information from the PlayerClient, and transfers it new actions.
 *
 * It is also responsible to draw the scene.
 */
trait GameHandler {

  protected val playerName: String
  protected val playersInfo: List[PlayerGameSettingsInfo]
  protected val client: PlayerClient
  val gameMode: GameMode

  def saveGameDataAndLoadScoreBoard(startTime: Long, deadPlayers: List[String]): Unit

  playersInfo.foreach(info => GameStatistics.newPlayer(
    info.id, info.playerName, info.team, Ability.abilityNames(info.abilities.head))
  )

  Engine.graphics.setBackgroundColor(0,0,0)

  val playerIds: List[Long] = playersInfo.map(_.id)
  val playerNames: List[String] = playersInfo.map(_.playerName)

  val gameClock: GameClock = new GameClock

  val teams: Map[Int, PlayerTeam] = playersInfo.groupBy(_.team).map(elem =>
    elem._1 -> new PlayerTeam(elem._2.head.team, elem._2.map(_.id))
  )

  val teamsByPlayerId: Map[Long, Int] = playersInfo.map(info => (info.id, info.team)).toMap


  val playerColors: Map[Long, (Double, Double, Double)] =
    playerIds.zip(GameHandler.definedColors.map(_.toRGB)).toMap

  val teamColors: Map[Int, (Double, Double, Double)] =
    teams.map(elem => elem._1 -> playerColors(elem._2.playerIds.head))

  val colorByPlayerName: Map[String, (Double, Double, Double)] =
    playerNames.zip(playerIds).map(elem => elem._1 -> playerColors(elem._2)).toMap

  val bulletColors: Map[Long, (Double, Double, Double)] =
    teams.flatMap({ case (teamNbr, team) => team.playerIds.map(id => (id, teamColors(teamNbr))) })

  val playerId: Long = playerIds(playerNames.indexOf(playerName))

  ScoreBoard
  playersInfo.foreach(info => {
    ScoreBoard.addPlayerLife(info.playerName, info.id, playerColors(info.id))
  })

  GunTurretHealthBar.setGameHandler(this)

//  /**
//   * Sends the key (or mouse button) to the ability index in the allowedAbilities list, after duplicates are removed.
//   */
//  private val abilityBinds: Map[String, Int] = Map(
//    "e" -> 0, "E" -> 0, "0" -> 0,
//    //"Mouse2" -> 1,
//    "a" -> 2, "A" -> 2, "1" -> 2,
//    "&" -> 3, "2" -> 3
//  )

  KeyBindingsLoader
  def bindings: ControlBindings = KeyBindingsLoader.bindings
  def isUpPressed: Boolean = (bindings.up._1 == ControlType.KeyboardType() && Engine.isDown(bindings.up._2)) ||
    (bindings.up._1 == ControlType.MouseType() && Engine.isMouseDown(bindings.up._2))
  def isDownPressed: Boolean = (bindings.down._1 == ControlType.KeyboardType() && Engine.isDown(bindings.down._2)) ||
    (bindings.down._1 == ControlType.MouseType() && Engine.isMouseDown(bindings.down._2))
  def isLeftPressed: Boolean = (bindings.left._1 == ControlType.KeyboardType() && Engine.isDown(bindings.left._2)) ||
    (bindings.left._1 == ControlType.MouseType() && Engine.isMouseDown(bindings.left._2))
  def isRightPressed: Boolean = (bindings.right._1 == ControlType.KeyboardType() && Engine.isDown(bindings.right._2)) ||
    (bindings.right._1 == ControlType.MouseType() && Engine.isMouseDown(bindings.right._2))

  def pressButton(controlType: ControlType, code: Int): Unit = {
    if (bindings.isBulletShootPressed(controlType, code)) {
      shootBullet()
    } else if (bindings.isSelectedAbilityPressed(controlType, code)) {
      useSelectedAbility()
    } else {
      bindings.isAbilityPressed(controlType, code) match {
        case -1 =>
        case idx => useAbility(idx, currentGameState)
      }
    }

  }


  private var lastShotTime: Long = 0

  def shootBullet(): Unit = {
    val gameState = currentGameState
    val now = Time.getTime

    if (now - lastShotTime > Bullet.reloadTime &&
      gameState.isPlayerAlive(playerId) && gameState.state == PlayingState) {
      val (mouseX, mouseY) = Engine.mousePosition
      val mousePos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
      val playerOpt = playerById(playerId, gameState)
      if (playerOpt.isDefined) {
        val player = playerOpt.get

        val rotation = (mousePos - player.pos).arg
        val startingPos = player.pos + Player.radius * Complex.rotation(rotation)

        client.sendAction(NewBullet(
          GameAction.newId(), Entity.newId(), playerId, teamsByPlayerId(playerId),
          startingPos, Bullet.defaultRadius, rotation, Bullet.speed, now, 0,
          PlayerSource
        ))

        lastShotTime = now
      }
    }
  }

  def useSelectedAbility(): Unit = {
    val gameState = currentGameState
    if (gameState.isPlayerAlive(playerId) && gameState.state == PlayingState) {
      val playerOpt = playerById(playerId, gameState)
      if (playerOpt.isDefined) {
        useAbility(
          playerOpt.get.allowedAbilities.distinct.indexOf(abilityButtons(focusBtnIndex).abilityId),
          gameState
        )
      }
    }
  }

  def useAbility(id: Int, gameState: GameState): Unit = if (gameState.isPlayerAlive(playerId)) {
    implicit def fromComplexToNetworkPoint(z: Complex): NetworkPoint = NetworkPoint(z.re, z.im)

    val player = gameState.players(playerId)
    val allowedAbilities = player.allowedAbilities.distinct
    if (allowedAbilities.lengthCompare(id) > 0) {
      // now the player has the ability, we can use it
      val abilityId = allowedAbilities(id)
      if (player.relevantUsedAbilities.values.filter(_.id == abilityId).forall(ability =>
        ability.cooldown / player.allowedAbilities.count(_ == abilityId) - 1000 < Time.getTime - ability.time
      )) { // the ability will be up in 1 second, we allow the client to send messages.
        abilityId match {
          case Ability.activateShieldId =>
            client.sendNormal(UseActivateShield(client.gameName, Time.getTime, 0, playerId))
          case Ability.bigBulletId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val rotation = (targetPos - player.pos).arg
            val startingPos = player.pos + Player.radius * Complex.rotation(rotation)
            client.sendNormal(UseBigBullet(client.gameName, Time.getTime, 0, playerId, teamsByPlayerId(playerId),
              startingPos.re, startingPos.im, rotation))
          case Ability.tripleBulletId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val rotation = (targetPos - player.pos).arg
            val startingPos = player.pos + Player.radius * Complex.rotation(rotation)
            client.sendNormal(UseTripleBullet(client.gameName, Time.getTime, 0, playerId, teamsByPlayerId(playerId),
              startingPos.re, startingPos.im, rotation))
          case Ability.teleportationId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val time = Time.getTime
            val ability = new Teleportation(time, 0, playerId, player.pos, targetPos)

            if (ability.isLegal(currentGameState)) {
              val message = UseTeleportation(client.gameName, time, 0, playerId, player.pos, targetPos)
              client.sendNormal(message)
              if (player.relevantUsedAbilities.values.filter(_.id == abilityId).forall(ability =>
                ability.time + ability.cooldown / player.allowedAbilities.count(_ == abilityId) < Time.getTime
              )) {
                unConfirmedActions = ability.createActions
              }
            }
          case Ability.createHealingZoneId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val time = Time.getTime
            val ability = new CreateHealingZone(time, 0, playerId, targetPos)

            if (ability.isLegal(currentGameState)) {
              val message = UseCreateHealingZone(client.gameName, time, 0, playerId, targetPos)
              client.sendNormal(message)
            }
          case Ability.createBulletAmplifierId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val time = Time.getTime
            val rotation = (targetPos - gameState.players(playerId).pos).arg
            val ability = new CreateBulletAmplifier(time, 0, playerId, targetPos, rotation)

            if (ability.isLegal(currentGameState)) {
              val message = UseCreateBulletAmplifier(client.gameName, time, 0, playerId,
                targetPos, rotation)
              client.sendNormal(message)
            }
          case Ability.launchSmashBulletId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val rotation = (targetPos - player.pos).arg
            val startingPos = player.pos + Player.radius * Complex.rotation(rotation)
            client.sendNormal(UseLaunchSmashBullet(client.gameName, Time.getTime, 0, playerId,
              startingPos, rotation))
          case Ability.craftGunTurretId =>
            client.sendNormal(
              UseCraftGunTurret(client.gameName, Time.getTime, 0, playerId, teamsByPlayerId(playerId), player.pos)
            )
          case Ability.createBarrierId =>
            val (mouseX, mouseY) = Engine.mousePosition
            val targetPos = EntityDrawer.camera.mousePosToWorld(Complex(mouseX, mouseY))
            val time = Time.getTime
            val rotation = (targetPos - gameState.players(playerId).pos).arg
            val ability = new CreateBarrier(time, 0, playerId, teamsByPlayerId(playerId), targetPos, rotation)

            if (ability.isLegal(currentGameState)) {
              val message = UseCreateBarrier(client.gameName, time, 0, playerId, teamsByPlayerId(playerId),
                targetPos, rotation)
              client.sendNormal(message)
            }
          case Ability.putBulletGlue =>
            client.sendNormal(UsePutBulletGlue(client.gameName, Time.getTime, 0, playerId, teamsByPlayerId(playerId)))
        }
      }
    }
  }


//  def useAbility(bind: String, gameState: GameState): Unit = abilityBinds.get(bind) match {
//    case Some(index) if gameState.isPlayerAlive(playerId) =>
//      useAbility(index, gameState)
//    case None =>
//  }



  def movePlayer(gameState: GameState, time: Long, dt: Double, mousePos: Complex, player: Player): UpdatePlayerPos = {
    val rotation = (mousePos - Complex(player.xPos, player.yPos)).arg

    var headingTo = Complex(0,0)



    if (isUpPressed) {
      headingTo += Complex(0, 200)
    }
    if (isDownPressed) {
      headingTo += Complex(0, -200)
    }
    if (isRightPressed) {
      headingTo += Complex(200, 0)
    }
    if (isLeftPressed) {
      headingTo += Complex(-200, 0)
    }

    val (moving, direction) = if (headingTo == Complex(0,0)) (false, 0.0) else (true, headingTo.arg)

//    val obstacles = gameState.obstacles.values
//    val enemyBarriers = gameState.barriers.values.filter(_.teamId != player.team)

    val obstaclesLike = gameState.collidingPlayerObstacles(player)

    val newPosition = if (moving) {

      val pos = player.lastValidPosition(
        player.pos + player.speed * dt / 1000 * Complex.rotation(direction), obstaclesLike
      )

      if (pos != player.pos)
        pos
      else {
        val secondTry = player.lastValidPosition(
          player.pos + player.speed * dt / 1000 * Complex.rotation(direction - math.Pi / 4), obstaclesLike
        )

        if (secondTry != player.pos)
          secondTry
        else
          player.lastValidPosition(
            player.pos + player.speed * dt / 1000 * Complex.rotation(direction + math.Pi / 4), obstaclesLike
          )
      }
    }
    else
      player.pos




    val newRotation = if (obstaclesLike.exists(obstacle =>
      player.shape.collides(newPosition, rotation, obstacle.shape, obstacle.pos, obstacle.rotation)
    )) {
      player.rotation
    } else {
      rotation
    }

    val finalMoving = player.pos != newPosition

    UpdatePlayerPos(
      GameAction.newId(), time, playerId, newPosition.re, newPosition.im, (newPosition - player.pos).arg,
      finalMoving, newRotation,
      PlayerSource
    )

  }



  protected def isPredictableAction(action: GameAction): Boolean = action match {
    case a: UpdatePlayerPos if a.playerId == playerId => true
    case _ => false
  }

  def addActions(actions: Seq[GameAction]): Unit = {
    if (actions.nonEmpty) {
      addAction(actions.head, actions.tail.isEmpty)
      addActions(actions.tail)
    }
  }

  def actionDenied(action: GameAction): Unit = action match {
    case UpdatePlayerPos(_, _, id, _, _, _, _, _, _) if id == playerId =>
      if (scala.scalajs.LinkingInfo.developmentMode) {
        dom.console.warn(s"An action has been denied [$action]")
      }
      unConfirmedActions = Nil // we remove all unConfirmedActions if an UpdatePlayerPos is denied, so that the client
                               // can synchronize again with the server
    case _ =>
  }

  def actionsDenied(actions: Seq[GameAction]): Unit =
    actions.foreach(actionDenied)

  def deleteActions(oldestTime: Long, actionIds: List[Long]): Unit = {
    actionCollector.removeActions(oldestTime, actionIds)
  }


  protected var abilityButtons: List[AbilityButton] = List()
  protected var focusBtnIndex: Int = 1

  protected def changeFocusedBtn(index: Int): Unit = {
    val mod = abilityButtons.length - 1
    val newIndex = (index + mod) % mod // -1 % n = -1 :'(
    abilityButtons(focusBtnIndex).blur()
    focusBtnIndex = newIndex
    abilityButtons(focusBtnIndex).focus()
  }

  protected def focusNextBtn(): Unit =
    changeFocusedBtn(focusBtnIndex - 1) // -1 because the abilityButtons list is sorted from right to left

  protected def focusPreviousBtn(): Unit =
    changeFocusedBtn(focusBtnIndex + 1)

  val watchingFrame: Frame = new Frame()
  watchingFrame.registerEvent(GameEvents.OnPlayerTakeAbilityGiver)(
    (action: PlayerTakeAbilityGiver, state: GameState) => {
    val pId = action.playerId
    val abilityId = action.abilityId
    if (pId == playerId && state.isPlayerAlive(playerId) && !abilityButtons.exists(_.abilityId == abilityId)) {
      abilityButtons +:= new AbilityButton(abilityId, playerId, Some(abilityButtons.head))
      focusPreviousBtn() // the new button shifts all the buttons to the right in indices
    }
  })



  val actionCollector: ActionCollector = new ActionCollector(GameState.originalState, 1000, 30000)
  protected var unConfirmedActions: List[GameAction] = Nil
  protected var buffer: List[GameAction] = Nil
  protected var lastUpdatePosAction: UpdatePlayerPos =
    UpdatePlayerPos(0, 0, playerId, 0, 0, 0, moving = false, 0, PlayerSource)
  protected var otherPlayersPredictions: Iterable[UpdatePlayerPos] = Nil


  protected var pendingActionHandler: Option[SetIntervalHandle] = None
  protected var gameEnded: Boolean = false

  protected var triggeredActions: List[GameAction] = Nil

  def addAction(action: GameAction, needUpdate: Boolean = true): Unit = {
    triggeredActions :+= action

    if (isPredictableAction(action)) {
      unConfirmedActions = unConfirmedActions.dropWhile(_.time < action.time)
    } else action match {
      case UseAbilityAction(_, _, ability, _, _)
        if ability.isInstanceOf[Teleportation] && ability.casterId == playerId =>
        unConfirmedActions = Nil
      case a: PlayerDead =>
        deadPlayers = playerNamesById(a.playerId) +: deadPlayers
        if (a.playerId == playerId) {
          unConfirmedActions = Nil
          buffer = Nil
          if (deadPlayers.lengthCompare(playerNames.length) < 0) {
            Engine.changeGameState(SpectatorModeRunner)
            Engine.startGameLoop()
          }
        }
        otherPlayersPredictions = otherPlayersPredictions.filter(_.playerId != a.playerId)
      case _: GameEndedAction =>
        val startTime = currentGameState.startTime.get

        unConfirmedActions = Nil
        clearInterval(pendingActionHandler.get)
        gameEnded = true

        actionCollector.addActions(triggeredActions)
        triggeredActions.foreach(GameEvents.fireGameEvent(_, currentGameState))
        triggeredActions = Nil

        if (currentGameState.players.nonEmpty) {
          deadPlayers = currentGameState.players.values.map(_.name).toList ++ deadPlayers
        }


        GameStatistics.saveGameStatistics(playerName, startTime, currentGameState)
        saveGameDataAndLoadScoreBoard(startTime, deadPlayers)



      case _: GameBegins =>
        currentGameState.players.keys.map(new PlayerHealthBar(_, this))
        currentGameState.players.values.foreach(player => playerNamesById += (player.id -> player.name))

        ScoreBoard.colorMap = client.gameHandler.colorByPlayerName

        gameClock.startClock()

        PlayerNameFS.hideFontStrings()

         pendingActionHandler = Some(setInterval(1000 / 15) {
          sendPendingActions()
        })
      case a: NewPlayer  =>
        if (a.player.id == playerId) {
          lastUpdatePosAction =
            UpdatePlayerPos(
              GameAction.newId(), a.time, playerId, a.player.xPos, a.player.yPos, 0, moving = false, 0, PlayerSource
            )
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
        PlayerNameFS.newName(
          a.player.name, a.player.pos + Complex(0, Player.radius + 2)// - EntityDrawer.camera.worldCenter
        )
      case _ =>
    }

  }

  def addUnConfirmedAction(action: GameAction): Unit =
    unConfirmedActions :+= action

  def playerById(id: Long, gameState: GameState = currentGameState): Option[Player] = gameState.players.get(id)

  private var deadPlayers: List[String] = Nil
  protected val playerNamesById: mutable.Map[Long, String] = mutable.Map[Long, String]()

  def sendPendingActions(): Unit = {
    //client.sendActions(buffer)
    if (buffer.nonEmpty) {
      client.sendAction(buffer.last)
    }
    buffer = Nil
  }


  def currentGameState: GameState = {
    // unConfirmedActions apply at the top of the game state, meaning that it is not in order, but it is much better
    // performance-wise and it's not a big deal for game rendering
    actionCollector.currentGameState(unConfirmedActions ++ otherPlayersPredictions)
  }

  val computationTimes: mutable.Queue[Int] = mutable.Queue()
  val computationTimesUpdateDraw: mutable.Queue[Int] = mutable.Queue()
  val renderTimes: mutable.Queue[Int] = mutable.Queue()
  val processActions: mutable.Queue[Int] = mutable.Queue()
  val dts: mutable.Queue[Int] = mutable.Queue()
  val inUpdateFunction: mutable.Queue[Int] = mutable.Queue()
  val inUpdateHandlers: mutable.Queue[Int] = mutable.Queue()
  val debugTime: mutable.Queue[Int] = mutable.Queue()


  val computationTimeFrame: Frame = new Frame()
  computationTimeFrame.setSize(150,40)
  computationTimeFrame.setPoint(BottomRight, UIParent, BottomRight)
  val computationTimeFS: FontString = computationTimeFrame.createFontString()
  computationTimeFS.setPoint(BottomRight, computationTimeFrame, Right)
  computationTimeFS.setSize(500, 20)

  if (!scala.scalajs.LinkingInfo.developmentMode) {
    computationTimeFrame.hide()
  }

  protected var lastComputationTime: Int = 0

  protected var actionsThatWhereOnStack: Int = 0
  protected var maxActionsOnStack: Int = 0
  val actionsOnStackFS: FontString = computationTimeFrame.createFontString()
  actionsOnStackFS.setPoint(TopRight, computationTimeFrame, Right)
  actionsOnStackFS.setSize(150,20)

  computationTimeFrame.setScript(ScriptKind.OnUIParentResize)(() => {
    computationTimeFrame.clearAllPoints()
    computationTimeFrame.setPoint(BottomRight, UIParent, BottomRight)
  })


  Engine.changeGameState(PreGameRunner)
  Engine.startGameLoop()


  val Runner: GameRunner




  /**
   * This state is set when the player dies. It can also be used if we want to implement spectator mode in the future.
   */
  object SpectatorModeRunner extends GameRunner {

    val run: Option[() => Unit] = None

    def draw(): Unit = {
      val (w, h) = Engine.graphics.dimensions
      val cameraPos = Complex(0, 0)

      EntityDrawer.camera.worldWidth = GameArea.sizeFromNbrPlayers(playerNames.length) * math.max(1, w / h.toDouble)
      EntityDrawer.camera.worldHeight = GameArea.sizeFromNbrPlayers(playerNames.length) * math.max(1, h.toDouble / w)

      val gameState = currentGameState
      val gameTime = if (gameEnded) gameState.time else Time.getTime

      EntityDrawer.drawState(cameraPos, gameState, gameTime, playerColors, teamColors, bulletColors)

      computationTimes.enqueue(Engine.computationTime.toInt)
      lastComputationTime = computationTimes.sum / computationTimes.size
      val t = lastComputationTime.toString + "ms"
      if (t != computationTimeFS.text) {
        computationTimeFS.setText(t)
      }

      if (computationTimes.lengthCompare(100) > 0) computationTimes.dequeue()
    }

    def keyPressed(key: String, keyCode: Int, isRepeat: Boolean): Unit = {
      Frame.keyPressed(key, keyCode, isRepeat)
    }

    def keyReleased(key: String, keyCode: Int): Unit = {

      Frame.keyReleased(key, keyCode)
    }

    def mousePressed(x: Double, y: Double, button: Int): Unit = {
      Frame.clickHandler(x, y, button)
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
      //triggeredActions.foreach(action => ScriptObject.firesEvent(GameEvents.OnActionTaken)(action, currentGameState))
      triggeredActions.foreach(GameEvents.fireGameEvent(_, currentGameState))
      triggeredActions = Nil

      otherPlayersPredictions = Nil
      val gameState = currentGameState

      if (ScoreBoard.isVisible)
        ScoreBoard.update(gameState)

      val time = Time.getTime
      otherPlayersPredictions = gameState.players.values.filter(_.id != playerId).filter(_.moving).map(
        player => {
          val pos = player.currentPosition(time - player.time)

          UpdatePlayerPos(
            GameAction.newId(), time, player.id, pos.re, pos.im, player.direction, player.moving, player.rotation,
            PlayerSource
          )
        }
      )


      Frame.updateHandler(dt)
    }
  }
}

object GameHandler {

  private val definedColors: List[Color] = List(
    Color(255, 0, 0),
    Color(100, 100, 255),
    Color(0, 255, 0),
    Color(204, 204, 0),
    Color(204, 0, 204),
    Color(102, 205, 170),
    Color(255, 165, 0)
  )

}


