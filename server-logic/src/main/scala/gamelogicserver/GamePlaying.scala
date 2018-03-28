package gamelogicserver

import abilities.{Ability, Teleportation}
import custommath.Complex
import entities._
import entitiescollections.PlayerTeam
import gamemessages.MessageMaker
import gamemode.GameMode
import gamestate.ActionSource.{PlayerSource, ServerSource}
import gamestate.GameState.{GameEnded, PlayingState}
import gamestate._
import gamestate.actions._
import networkcom._
import networkcom.messages._
import org.scalajs.dom
import org.scalajs.dom.html
import time.Time

import scala.scalajs.js.timers._
import scala.collection.mutable


/**
 * A GamePlaying manages what happens during a Game.
 * It receives input from the players, and manages all collisions during the game.
 */
// TODO: see what methods must remain here and those that need to be in children classes
trait GamePlaying {

  val gameName: String
  val password: Int
  val gameMode: GameMode
  protected val playersInfo: Vector[PlayerGameSettingsInfo]
  protected val server: GameLogicServer


  val desiredFPS: Int = 120


  private def actionId(): Long = GameAction.newId()


  /**
   * Closes the game, either if it is actually finished, or if all players have been disconnected.
   * What makes a game finished depends on the GameMode.
   *
   * @param msg A message specifying why the game is closed.
   */
  def closeGame(msg: String): Unit = {
    if (state != GamePlayingEnded) {
      broadcastReliable(ClosingGame(gameName, msg))
    }
  }

  /**
   * Manages what happens when the player with peer is disconnected.
   * For now, it dies and is just removed from the game, but we will eventually need to allow them to reconnect.
   */
  def playerDisconnected(peer: Peer): Unit = peerToPlayer.get(peer) match {
    case Some(playerName) =>
      playersWithPeers -= playerName
      gameState.players.values.find(_.name == playerName) match {
        case Some(player) =>
          queueActions(List(PlayerDead(actionId(), Time.getTime, player.id, player.name, ServerSource)))
          if (gameState.state == GameEnded) {
            _state = GamePlayingEnded
          }
        case None =>
      }
    case None =>
  }



  /**
   * The teams identify groups of players that play on the same side.
   * It maps the team number to the PlayerTeam object containing all the players ids.
   */
  val teams: Map[Int, PlayerTeam] = playersInfo.groupBy(_.team).map(elem =>
    elem._1 -> new PlayerTeam(elem._2.head.team, elem._2.map(_.id))
  )

  /**
   * Maps the ids of the players to their team.
   */
  val teamsByPlayerId: Map[Long, PlayerTeam] = playersInfo.map(info => info.id -> teams(info.team)).toMap

  val gameArea: GameArea
  val gameAreaSideLength: Int = GameArea.sizeFromNbrPlayers(playersInfo.length)

  /**
   * State of the game. It first wait for players.
   */
  protected var _state: GamePlayingState = WaitingForPlayers

  def state: GamePlayingState = _state

  /**
   * The players with Peers map is filled when they connect to the game.
   */
  private val playersWithPeers: mutable.Map[String, Peer] = mutable.Map()
  def playersPeers: Iterable[Peer] = playersWithPeers.values
  def peerToPlayer: Map[Peer, String] = playersWithPeers.toSet.map(
    (elem: (String, Peer)) => (elem._2, elem._1)
  ).toMap

  /**
   * Sends a reliable message to all game members.
   */
  protected def broadcastReliable(message: InGameMessage): Unit = {
    playersWithPeers.values.foreach(server.sendReliable(message, _))
  }

  /**
   * Sends an ordered reliable message to all game members.
   */
  private def broadcastOrderedReliable(message: InGameMessage): Unit = {
    playersWithPeers.values.foreach(server.sendOrderedReliable(message, _))
  }

  /**
   * Manages what happens when a game message is received.
   */
  def messageCallback(message: InGameMessage, peer: Peer): Unit = if (state != GamePlayingEnded) {
    message match {
      case message: UseAbility =>
        receiveAbility(MessageMaker.messageToAbility(message))

      case message: ActionMessage =>
        val isAccepted: Boolean = receiveAction(MessageMaker.messageToAction(message))
        if (!isAccepted) {
          server.sendOrdered(ActionDenied(gameName, message), peer)
        }

      case ActionsMessage(_, actions) =>
        receiveActions(actions.map(MessageMaker.messageToAction), peer)

      case PlayerConnecting(gName, pName, pw) if gameName == gName && password == pw &&
        playersInfo.exists(pName == _.playerName)
      =>
        playersWithPeers += (playersInfo.find(_.playerName == pName).get.playerName -> peer)
        val stillWaitFor = playersInfo.size - playersWithPeers.size
        if (stillWaitFor > 0) {
          broadcastOrderedReliable(StillWaitingForPlayers(gameName, stillWaitFor))
        } else {
          broadcastOrderedReliable(GameStarts(gameName))
          _state = PreGame
          setTimeout(1000) {
            performAction()
          }
        }

      case PlayerConnecting(gName, pName, pw) =>
        if (gName != gameName) {
          dom.console.warn(s"received PlayerConnecting but with game name $gName instead of $gameName...")
        } else if (pw != password) {
          dom.console.warn(s"received PlayerConnecting but with password $pw instead of $password...")
        } else {
          dom.console.warn(s"received PlayerConnecting from $pName, but they are not in the game...")
        }

      case GuessClockTime(gName, time) =>
        server.sendReliable(AnswerGuessClockTime(gName, time, Time.getTime), peer)

      case InGameChatMessage(g, s, _, p) =>
        if (s.trim != "") {
          // the time of the message is the instant at which the server receives it
          broadcastReliable(InGameChatMessage(g, s, Time.getTime, p))
        }

      case _ =>
        println(s"Unknown message: $message")
    }
  }


  /**
   * The ActionCollector of the server. See the class itself for explanation.
   */
  val actionCollector: ActionCollector = new ActionCollector(GameState.originalState, 500, 60000)

  /** Facility method to retrieve the GameState. */
  def gameState: GameState = actionCollector.currentGameState

  /**
   * All actions are stored in the newActions queue.
   * When the server sends actions back to the clients, the newActions queue is emptied.
   */
  private val newActions: mutable.Queue[GameAction] = mutable.Queue()


  private var actionsToBeRemoved: List[(Long, List[Long])] = Nil

  /**
   * Queue all actions to be sent to the clients, and updates the GameState via the ActionCollector.
   */
  def queueActions(actions: Iterable[GameAction]): Unit = {
    if (actions.nonEmpty) {

      // testing the fact of removing actions
//      actions.find(_.isInstanceOf[PlayerHitByMultipleBullets]) match {
//        case Some(action) =>
//          println(action)
//          println("removing in 3s")
//          scala.scalajs.js.timers.setTimeout(3000) {
//            println("removing")
//            actionCollector.removeActions(action.time, List(action.actionId))
//            broadcastOrderedReliable(
//              DeleteActions(gameName, action.time, List(action.actionId))
//            )
//          }
//        case None =>
//      }

//      actionCollector.addActions(actions)
      val (oldestTime, removed) = actionCollector.addAndRemoveActions(actions.toList)
      actions.filterNot(action => removed.contains(action.actionId)).foreach(newActions.enqueue(_))
      if (removed.nonEmpty) {
        println("were removed:")
        println(removed.mkString(", "))

        actionsToBeRemoved = actionsToBeRemoved :+ (oldestTime, removed)
      }
    }
  }

  def queueAction(action: GameAction): Unit = {
    newActions.enqueue(action)
    actionCollector.addAction(action)
  }

  /**
   * Empties the newActions queue and send all actions to the clients.
   * We do not do it if newActions is empty, to try to limit the use of bandwidth.
   */
  def broadcastActions(): Unit = {
    if (newActions.nonEmpty) {
      val buffer: List[GameAction] = {
        var acc: List[GameAction] = Nil
        while (newActions.nonEmpty) acc +:= newActions.dequeue()
        acc.reverse
      }
      actionsToBeRemoved.foreach {
        case (oldestTime, actionIds) =>
          broadcastOrderedReliable(DeleteActions(gameName, oldestTime, actionIds))
      }
      actionsToBeRemoved = Nil
      broadcastOrderedReliable(MessageMaker.actionsMessage(gameName, buffer))
    }
  }

  protected var setIntervalHandlers: List[SetIntervalHandle] = Nil


  protected def manageGunTurret(time: Long): Unit = if (gameState.gunTurrets.nonEmpty) {

    // smash bullet do not hit gun turrets by design

    queueActions(
      gameState.bullets.values.flatMap(bullet => {
        gameState.gunTurrets.values
          .filterNot(turret => teamsByPlayerId(bullet.ownerId).contains(turret.ownerId))
          .find(turret => turret.collides(bullet, time - bullet.time)) match {
          case Some(turret) =>
            val damage = Bullet.damageFromRadius(bullet.radius)
            if (turret.lifeTotal <= damage) {
              List(
                DestroyBullet(actionId(), bullet.id, time, ServerSource),
                DestroyGunTurret(actionId(), time, turret.id, ServerSource)
              )
            } else {
              List(
                DestroyBullet(actionId(), bullet.id, time, ServerSource),
                GunTurretTakesDamage(actionId(), time, turret.id, damage, ServerSource)
              )
            }
          case None =>
            Nil
        }
      })
    )

    queueActions(
      gameState.gunTurrets.values
        .filter(time - _.lastShot > GunTurret.shootRate)
        .flatMap(turret => {
          gameState.players.values
            .filterNot(teamsByPlayerId(turret.ownerId).contains)
            .map(player => (player, (turret.pos - player.currentPosition(time - player.time)).modulus2))
            .filter(_._2 < GunTurret.defaultReach * GunTurret.defaultReach)
            .toList match {
            case Nil =>
              None
            case list =>
              val target = list.minBy(_._2)._1
              val rotation = (target.pos - turret.pos).arg

              Some(GunTurretShoots(
                actionId(), time, turret.id, rotation, Entity.newId(), Bullet.defaultRadius, Bullet.speed, ServerSource
              ))
          }
        })
    )

  }

  protected def manageHealingZones(time: Long): Unit = {
    queueActions(
      gameState.healingZones.values
        .filter(time - _.lastTick > HealingZone.tickRate)
        .flatMap(zone => {
          val team = teamsByPlayerId(zone.ownerId)
          UpdateHealingZone(
            actionId(), time, zone.id, zone.ownerId, zone.lifeSupply, zone.xPos, zone.yPos, ServerSource
          ) +:
          team.playerIds.filter(gameState.isPlayerAlive).map(gameState.players(_))
            .filter(player => player.collides(zone, time - player.time))
            .map(player => HealingZoneHeals(
              actionId(), time, player.id, zone.id, HealingZone.healingOnTick, ServerSource)
            )
            .take(zone.ticksRemaining)
        })
    )

    queueActions(
      gameState.healingZones.values
        .filter(zone => zone.lifeSupply <= 0 || time - zone.creationTime > HealingZone.lifetime)
        .map(zone => DestroyHealingZone(actionId(), time, zone.id, ServerSource))
    )
  }

  protected def manageSmashBullets(time: Long): Unit = {

    queueActions(
      gameState.smashBullets.values
        .filter(bullet =>
          bullet.currentTravelledDistance(time) > SmashBullet.reach
        )
        .map(bullet => DestroySmashBullet(actionId(), time, bullet.id, ServerSource))
    )

    queueActions(
      gameState.smashBullets.values
        .filter(time - _.lastGrow > SmashBullet.growRate)
        .map(bullet => {
          SmashBulletGrows(actionId(), time, bullet.id, bullet.radius + SmashBullet.growValue, ServerSource)
        })
    )

    queueActions(
      gameState.smashBullets.values.flatMap(bullet => {
        gameState.players.values
          .filterNot(_.team == teamsByPlayerId(bullet.ownerId).teamNbr)
          .find(player => player.collides(bullet, time - player.time, time - bullet.time)) match {
          case Some(player) =>
            List(
              PlayerHitBySmashBullet(actionId(), time, player.id, bullet.id, ServerSource),
              DestroySmashBullet(actionId(), time, bullet.id, ServerSource)
            )
          case None =>
            Nil
        }

      })
    )

  }

  protected def manageBulletAmplifiers(time: Long): Unit = if (gameState.bulletAmplifiers.nonEmpty) {

    val bulletsByOwner = gameState.bullets.values.groupBy(_.ownerId)

    queueActions(
      gameState.bulletAmplifiers.values.flatMap(bulletAmplifier => {
        val team = teamsByPlayerId(bulletAmplifier.ownerId)
        team.playerIds.flatMap(id => {
          bulletsByOwner.get(id) match {
            case Some(bullets) =>
              bullets
                .filter(bullet => bullet.collides(bulletAmplifier, time - bullet.time))
                .filterNot(bullet => bulletAmplifier.isBulletAmplified(bullet.id))
                .flatMap(bullet => List(
                  ChangeBulletRadius(actionId(), time, bullet.id, 2 * bullet.radius, ServerSource),
                  BulletAmplifierAmplified(actionId(), time, bullet.id, bulletAmplifier.id, ServerSource)
                ))
            case None =>
              Nil
          }
        })
      })
    )

    queueActions(
      gameState.bulletAmplifiers.values
        .filter(time - _.creationTime > BulletAmplifier.lifeTime)
        .map(bA => DestroyBulletAmplifier(actionId(), time, bA.id, ServerSource))
    )

  }

  protected def manageBarriers(time: Long): Unit = {

    queueActions(
      gameState.barriers.filter(time - _._2.time > Barrier.lifeTime).keys.map(
        DestroyBarrier(actionId(), time, _, ServerSource)
      )
    )

    gameState.barriers.values.foreach {
      barrier =>
        queueActions(
          gameState.players.values
            .filter(_.team != barrier.teamId)
            .filter(_.collides(barrier))
            .map(player =>
              (
                player,
                player.firstValidPosition((player.pos - barrier.pos).arg, gameState.collidingPlayerObstacles(player))
              )
            )
            .map({ case (player, position) =>
              UpdatePlayerPos(
                actionId(), time, player.id, position.re, position.im, player.direction,
                moving = false, player.rotation, ServerSource
              )
            })
        )
    }

  }


  protected def manageActionChangers(time: Long): Unit = {
    queueActions(
      gameState.actionChangers.values.toList
        .filter(changer => time - changer.time > changer.duration)
        .map(changer => ActionChangerEnded(actionId(), time, changer.id, ServerSource))
    )
  }

  protected def manageHealUnits(time: Long): Unit = {
    // removing old HealUnit
    queueActions(
      gameState.healUnits.values.toList
        .filter(time - _.time > HealUnit.lifeTime)
        .map(unit => DestroyHealUnit(actionId(), time, unit.id, ServerSource))
    )

    // player use heal unit if they collide with it
    queueActions(
      gameState.healUnits.values.map(unit =>
      gameState.players.values.find(player => unit.collides(player, time - player.time)) match {
        case Some(player) =>
          Some(PlayerTakeHealUnit(actionId(), time, player.id, unit.id, ServerSource))
        case None =>
          None
      }
      )
      .filter(_.isDefined).map(_.get)
    )

  }

  protected def manageAbilityGivers(time: Long): Unit = {
    queueActions(
      gameState.abilityGivers.values.flatMap(abilityGiver => {
        gameState.players.values.find(player => abilityGiver.collides(player, time - player.time)) match {
          case Some(player) =>
            Some(PlayerTakeAbilityGiver(
              actionId(), time, player.id, abilityGiver.id, abilityGiver.abilityId, ServerSource
            ))
          case None =>
            None
        }
      })
    )
  }

  protected def manageDamageZones(time: Long): Unit = {
    // checking growing DamageZones
    queueActions(
      gameState.damageZones.values.toList
        .filter(time - _.lastGrow > DamageZone.growingRate)
        .map(zone => UpdateDamageZone(
          actionId(), time, zone.id, time, zone.lastTick, zone.xPos, zone.yPos,
          zone.radius + DamageZone.growingValue, ServerSource
        ))
    )

    //checking collision Player <-> Zone
    queueActions(
      gameState.damageZones.values.toList
        .filter(time - _.lastTick > DamageZone.tickRate)
        .flatMap(zone => {
          UpdateDamageZone(
            actionId(), time, zone.id, zone.lastGrow, time, zone.xPos, zone.yPos, zone.radius, ServerSource
          ) +:
            gameState.players.values.toList
              .filter(player => player.collides(zone, time - player.time))
              .map(player => PlayerTakeDamage(
                actionId(), time, player.id, zone.id, DamageZone.damageOnTick, ServerSource
              ))
        })
    )

    // checking for too big DamageZone
    queueActions(
      gameState.damageZones.values.toList
        .filter(_.radius > DamageZone.maxRadius)
        .map(zone => DestroyDamageZone(actionId(), time, zone.id, ServerSource))
    )
  }

  protected def manageMists(time: Long): Unit = {
    // checking if the Mist hits someone
    queueActions(
      gameState.mists.values.toList
        .filter(time - _.lastTick > Mist.tickRate)
        .flatMap(mist => {
          UpdateMist(
            actionId(), time, mist.id, mist.lastGrow, time, mist.sideLength, gameAreaSideLength, ServerSource
          ) +:
            gameState.players.values.toList
              .filter(player => player.collides(mist, time - player.time))
              .map(player => PlayerTakeDamage(actionId(), time, player.id, mist.id, Mist.damagePerTick, ServerSource))
        })
    )

    // checking if the Mist needs to grow
    queueActions(
      gameState.mists.values.toList
        .filter(_.sideLength > Mist.minGameSide)
        .filter(time - _.lastGrow > Mist.growthRate)
        .map(mist => {
          UpdateMist(actionId(), time, mist.id, time, mist.lastTick, Mist.shrinkFunction(
            time, gameState.startTime.get, gameAreaSideLength
          ), gameAreaSideLength, ServerSource)
        })
    )
  }

  protected def manageRelevantAbilities(time: Long): Unit = {
    queueActions(
      gameState.withAbilities.values
        .flatMap(entity => entity.relevantUsedAbilities.values
          .filter(ability => ability.time + ability.cooldown < time)
          .map(ability => RemoveRelevantAbility(actionId(), time, entity.id, ability.useId, ServerSource))
        )
    )
  }

  protected def manageBullets(time: Long): Unit = {

    // killing bullets that went to far or hit an obstacle
    val (deadBullets, aliveBullets) = gameState.bullets.values.toList
      .partition(bullet => {
        gameState.collidingPlayerObstacles(bullet.teamId)
          .exists(_.collides(bullet, time - bullet.time)) || bullet.currentTravelledDistance(time) > Bullet.reach
      })

    queueActions(deadBullets.map(bullet => DestroyBullet(actionId(), bullet.id, time, ServerSource)))

    // looking for players hit by bullet
    queueActions(
      gameState.players.values.toList
        .map(player => (player, player.currentPosition(time - player.time)))
        .map({
          case (player, playerPos) =>
            (
              aliveBullets
                .filter(bullet => bullet.teamId != player.team)
                .filter(bullet => {
                  bullet.shape.collides(
                    bullet.currentPosition(time - bullet.time), 0, player.shape, playerPos, player.rotation
                  )
                }),
              player.id
            )
        })
        .filter(_._1.nonEmpty)
        .map({
          case (collidingBullets, playerId) =>
          PlayerHitByMultipleBullets(
            actionId(),
            time,
            collidingBullets.map(_.id),
            playerId,
            collidingBullets.map(bullet => Bullet.damageFromRadius(bullet.radius)).sum,
            ServerSource
          )
        })
    )
  }


  protected def manageDeadPlayers(time: Long): Unit = {
    queueActions(gameState.players.values.filter(_.lifeTotal <= 0).flatMap(player =>
      gameState.laserLaunchers.values.find(_.ownerId == player.id) match {
        case None => List(
          PlayerDead(actionId(), time, player.id, player.name, ServerSource),
          NewAbilityGiver(actionId(), time, Entity.newId(), player.pos, player.allowedAbilities(1), ServerSource)
        )
        case Some(laserLauncher) => List(
          PlayerDead(actionId(), time, player.id, player.name, ServerSource),
          NewAbilityGiver(actionId(), time, Entity.newId(), player.pos, player.allowedAbilities(1), ServerSource),
          DestroyLaserLauncher(GameAction.newId(), time, laserLauncher.id, ServerSource)
        )
      }
    ))

  }

  private val computationTimes: mutable.Queue[Long] = mutable.Queue()
  (1 to 100).foreach((_: Int) => computationTimes.enqueue(1))

  protected def isGameOver: Boolean

  protected def managePlayingState(time: Long): Unit

  protected def managePreGameState(): Unit

  protected def manageGamePlayingEnded(): Unit

  protected def performAction(): Unit = {
    _state match {
      case Playing =>
        val time = Time.getTime
        managePlayingState(time)
        val computationTime = Time.getTime - time

        val disconnectedPlayers = playersLiveStatus.filter(time - _._2 > 1000).keys

        if (disconnectedPlayers.nonEmpty) {
          queueActions(
            disconnectedPlayers
              .filter(gameState.isPlayerAlive)
              .map(gameState.players)
              .map(
                player => UpdatePlayerPos(
                  actionId(), time, player.id, player.xPos, player.yPos, 0,
                  moving = false, player.rotation, ServerSource
                )
              )
          )
        }

        computationTimes.dequeue()
        computationTimes.enqueue(computationTime)
        dom.document.getElementById("computationTime").asInstanceOf[html.Paragraph].textContent =
          "Average computation time: " + computationTimes.sum / 100.0

        val nextAction = 1000 / desiredFPS - computationTime

        if (isGameOver) {
          _state = GamePlayingEnded
          performAction()
        } else {
          setTimeout(math.max(10, nextAction)) {
            performAction()
          }
        }

      case PreGame =>
        managePreGameState()

      case GamePlayingEnded =>
        manageGamePlayingEnded()
        // sending that the game has ended, with the winner
        //clearInterval(gameLoopHandler.get)
        setIntervalHandlers.foreach(clearInterval)

        newActions.enqueue(GameEndedAction(actionId(), Time.getTime + 1, ServerSource))
        broadcastActions()

        println("game has ended")

        server.closePlayingGame(gameName)

      case WaitingForPlayers =>
        // do nothing
        // we should never come here, but the compiler would complain if we don't put this line.
    }
  }

  private val experiencedDelays: mutable.Queue[Long] = mutable.Queue()


  /**
   * We record each time we get a message from a player.
   * If a player does not send message for an entire second, we put them not moving at the last known position.
   */
  private val playersLiveStatus: mutable.Map[Long, Long] =
    playersInfo.map(_.id).foldLeft(mutable.Map[Long, Long]())({ case (currentMap, id) =>
      currentMap += (id -> 0)
      currentMap
    })

  /**
   * We record each time we get a bullet message from a player.
   * If a player sends two messages separated by less than 1/11 second, we reject the second.
   */
  private val lastBulletsSent: mutable.Map[Long, Long] =
    playersInfo.map(_.id).foldLeft(mutable.Map[Long, Long]())({ case (currentMap, id) =>
        currentMap += (id -> 0)
        currentMap
    })

  private val bulletReloadTime: Long = Bullet.reloadTime


  /**
   * Manages what needs to be done when receiving an action.
   *
   * @param action the [[GameAction]] received.
   * @return       true if the action was accepted, false otherwise.
   */
  def receiveAction(action: GameAction): Boolean = if (gameState.state == GameEnded) false
  else {
    experiencedDelays.enqueue(Time.getTime - action.time)

    if (experiencedDelays.lengthCompare(100) > 0) {
      dom.document.getElementById("actionDelay").asInstanceOf[html.Paragraph].textContent =
        s"Average delay on actions: ${experiencedDelays.sum / experiencedDelays.size}"
      experiencedDelays.dequeue()
    }

    if (scala.scalajs.LinkingInfo.developmentMode) {
      val time = Time.getTime
      if (time < action.time) {
        println(action.time - time)
      }
    }

    val now = Time.getTime

    // Not accepting actions holder than 1s.
    if (math.abs(now - action.time) > 1000) {
      dom.console.warn(s"Received too old action $action")
      false
    } else if (gameState.state == PlayingState &&
      actionCollector.gameStateUpTo(math.max(action.time, now - 1)).isLegalAction(action)) {
      (if (now - 1 > action.time) action else action.changeTime(now - 1)) match {
        case action: NewBullet if action.time - lastBulletsSent(action.playerId) > bulletReloadTime =>
          lastBulletsSent += action.playerId -> action.time
          val newAction = NewBullet(
            actionId(), Entity.newId(), action.playerId, action.teamId,
            action.pos, action.radius, action.dir, action.speed, action.time, 0,
            PlayerSource
          )
          queueAction(newAction.setId(GameAction.newId()))
          true
        case action: UpdatePlayerPos if gameState.isPlayerAlive(action.playerId) =>
          playersLiveStatus += action.playerId -> Time.getTime

          val player = gameState.players(action.playerId)
          if (gameState.collidingPlayerObstacles(player).exists(
            obs => obs.shape.collides(obs.pos, obs.rotation, player.shape, Complex(action.x, action.y), action.rot)
          )) {
            dom.console.warn("Player collides a Barrier")
            false
          } else {
            val newAction = action
            queueAction(newAction.setId(GameAction.newId()))
            true
          }
        case _ =>
          dom.console.warn(s"Received this message: $action")
          false
      }
    } else false
  }


  def receiveActions(actions: Seq[GameAction], peer: Peer): Unit = {
    // this is not very beautiful from a Functional point of view, but let's pretend it is.
    val deniedActions = actions.filterNot(receiveAction).map(MessageMaker.toMessage(gameName, _))

    if (deniedActions.nonEmpty) {
      server.sendOrdered(ActionsDenied(gameName, deniedActions.toList), peer)
    }
  }



  def receiveAbility(ability: Ability): Unit = {
    val now = Time.getTime
    if (ability.time > now - 1000) {
      val currentGameState = actionCollector.gameStateUpTo(ability.time)
      if (gameState.state == PlayingState && currentGameState.isLegalAbilityUse(ability)) {
        val abilityTime = if (now > ability.time || ability.isInstanceOf[Teleportation]) ability.time else now
        val newAbility = ability.copyWithUseId(Ability.newId(), abilityTime)
        val abilityActions =
          UseAbilityAction(actionId(), abilityTime, newAbility, newAbility.useId, PlayerSource) +:
            newAbility.createActions(currentGameState)
        queueActions(abilityActions)
      }
    }
  }

  def receiveAbilities(abilities: Seq[Ability]): Unit = {
    if (abilities.nonEmpty) {
      receiveAbility(abilities.head)
      receiveAbilities(abilities.tail)
    }
  }



  /** Find a random position away from the players */
  def findPosition(minDistSquared: Int = 10000, attempt: Int = 0): Option[(Double, Double)] = {
    if (attempt < 50) {
      val (x, y) = gameArea.randomPos()
      if (gameState.players.values.forall(player => (player.pos - Complex(x, y)).modulus2 > minDistSquared))
        Some((x, y))
      else
        findPosition(minDistSquared, attempt + 1)
    } else {
      None
    }
  }

  def findPosition(center: Complex, width: Double, height: Double, minDistSquared: Int, attempt: Int):
  Option[(Double, Double)] = {
    if (attempt < 50) {
      val (x, y) = gameArea.randomPos(center, width, height)
      if (gameState.players.values.forall(player => (player.pos - Complex(x, y)).modulus2 > minDistSquared))
        Some((x, y))
      else
        findPosition(minDistSquared, attempt + 1)
    } else {
      None
    }

  }

  def findPositionInMist(mist: Mist, minDistSquared: Int = 10000, attempt: Int = 0): Option[(Double, Double)] = {
    if (attempt < 50) {
      val (x, y) = gameArea.randomPos(mist)
      if (gameState.players.values.forall(player => (player.pos - Complex(x, y)).modulus2 > minDistSquared))
        Some((x, y))
      else
        findPositionInMist(mist, minDistSquared, attempt + 1)
    } else {
      None
    }
  }



  def addZone(gameState: GameState): Unit = {
    // we only add zones in the mist-clear area
    (if (gameState.mists.isEmpty)
      findPosition()
    else
      findPositionInMist(gameState.mists.values.head)) match {
      case Some((x, y)) =>
        val time = Time.getTime
        val action = UpdateDamageZone(
          actionId(), time, Entity.newId(), time, time, x, y, DamageZone.startingRadius, ServerSource
        )
        newActions.enqueue(action)
        actionCollector.addAction(action)
      case None =>
      // did not find a suitable place to put the danger zone, just cancel for this time
    }
  }

  def addHealUnit(): Unit = {
    // we only add heal units in the mist-clear area
    (if (gameState.mists.isEmpty)
      findPosition()
    else
      findPositionInMist(gameState.mists.values.head)) match {
      case Some((x, y)) =>
        val action = NewHealUnit(actionId(), Time.getTime, Entity.newId(), Complex(x, y), ServerSource)
        newActions.enqueue(action)
        actionCollector.addAction(action)
      case None =>
      // did not find a suitable place to put the heal unit, just cancel for this time
    }
  }

}



sealed trait GamePlayingState
case object WaitingForPlayers extends GamePlayingState
case object PreGame extends GamePlayingState
case object Playing extends GamePlayingState
case object GamePlayingEnded extends GamePlayingState
