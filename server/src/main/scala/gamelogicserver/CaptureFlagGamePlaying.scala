package gamelogicserver

import custommath.Complex
import entities._
import entitiescollections.CaptureTheFlagInfo
import gamemode.{CaptureTheFlagMode, GameMode}
import gameserver.GameServer
import gamestate.ActionSource.ServerSource
import gamestate.{GameAction, GameState}
import gamestate.actions._
import gamestate.actions.{NewPlayer => NewPlayerAction}
import networkcom.PlayerGameSettingsInfo
import networkcom.messages.GameStartsIn
import physics.ConvexPolygon
import time.Time

import scala.collection.mutable
import scala.scalajs.js.timers.{SetTimeoutHandle, setInterval, setTimeout}


/**
 * Manages the game during a Capture the Flag game.
 *
 * This in part is focused on 2 teams battle, but with some of it implemented in a general setting, maybe for later.
 */
class CaptureFlagGamePlaying(val gameName: String,
                             val password: Int,
                             protected val playersInfo: Vector[PlayerGameSettingsInfo],
                             protected val server: GameServer) extends GamePlaying {

  val gameMode: GameMode = CaptureTheFlagMode

  val captureTheFlagInto: CaptureTheFlagInfo = new CaptureTheFlagInfo(
    playersInfo.map(info => CaptureTheFlagInfo.PlayerInfo(info.playerName, info.abilities, info.team, info.id))
  )

  val gWidth: Int = captureTheFlagInto.gWidth
  val gHeight: Int = captureTheFlagInto.gHeight
  val teamZoneWidth: Int = captureTheFlagInto.teamZoneWidth
  val gameArea: GameArea = captureTheFlagInto.gameArea
  val teamNumbers: List[Int] = captureTheFlagInto.teamNumbers
  val flagZones: Map[Int, Zone] = captureTheFlagInto.flagZones
  val popPositions: Map[Int, Complex] = captureTheFlagInto.popPositions

  val turretIds: Map[Long, (Int, Complex)] = Map( // only managed for 2 teams
    Entity.newId() -> (teamNumbers.head, Complex(-gWidth / 2 + teamZoneWidth - 2 * GunTurret.defaultRadius, -400)),
    Entity.newId() -> (teamNumbers.head, Complex(-gWidth / 2 + teamZoneWidth - 2 * GunTurret.defaultRadius, 400)),
    Entity.newId() -> (teamNumbers.tail.head, Complex(gWidth / 2 - teamZoneWidth + 2 * GunTurret.defaultRadius, -400)),
    Entity.newId() -> (teamNumbers.tail.head, Complex(gWidth / 2 - teamZoneWidth + 2 * GunTurret.defaultRadius, 400))
  )


  private var setTimeoutHandles: List[SetTimeoutHandle] = Nil


  private var scores: Map[Int, Int] = teams.mapValues(_ => 0)

  def isGameOver: Boolean = if (scala.scalajs.LinkingInfo.developmentMode) {
    scores.values.exists(_ == 1)
  } else
    scores.values.exists(_ == 3)


  override def manageDeadPlayers(time: Long): Unit = {
    val dead = gameState.players.values.filter(_.lifeTotal <= 0).map(player => {
      PlayerDead(GameAction.newId(), time, player.id, player.name, ServerSource)
    })

    queueActions(
      gameState.flags.values
        .filter(_.isBorn)
        .filter(flag => dead.exists(_.playerId == flag.bearerId.get))
        .map(flag => PlayerDropsFlag(GameAction.newId(), time, flag.id, ServerSource))
    )

    queueActions(dead)
    dead.foreach(action => {
      setTimeoutHandles +:= setTimeout(CaptureTheFlagInfo.resurrectionTime) {
        val player = gameState.deadPlayers(action.playerId)
        val pos = popPositions(player.team)
        queueActions(List(
          NewPlayerAction(GameAction.newId(), new Player(
            player.id, player.team, Time.getTime, player.name, pos.re, pos.im, 0, Player.speed,
            moving = false, 0, player.shape, 100, player.allowedAbilities, Map()), Time.getTime, ServerSource)
        ))
      }
    })
  }

  def manageTeamFlags(time: Long): Unit = {

    val (bornFlags, onTheGroundFlags) = gameState.flags.values.partition(_.isBorn)

    val broughtBackFlags = bornFlags
      .map(flag => (flag, gameState.players(flag.bearerId.get)))
      // not looking at flags if the bearer team flag is born
      .filter(couple => !gameState.flags(couple._2.team).isBorn)
      .filter(couple => flagZones(couple._2.team).collides(couple._2, time - couple._2.time))
      .map({ case (flag, player) => PlayerBringsFlagBack(GameAction.newId(), time, flag.id, player.id, ServerSource) })

    queueActions(broughtBackFlags)

    scores = TeamFlag.scores(gameState)

    /** Checking if a player takes an enemy flag. */
    queueActions(
      onTheGroundFlags
        .flatMap(flag => gameState.players.values
          .filter(_.team != flag.teamNbr)
          .find(player => flag.collides(player, time - player.time)) match {
          case Some(player) =>
            Some(PlayerTakesFlag(GameAction.newId(), time, flag.id, player.id, ServerSource))
          case _ =>
            None
        })
    )


  }

  private val buildTurretsHandles: mutable.Map[Long, SetTimeoutHandle] = mutable.Map()

  override def manageGunTurret(time: Long): Unit = {
    super.manageGunTurret(time)

    turretIds
      .filter(elem => !gameState.gunTurrets.isDefinedAt(elem._1))
      .filter(elem => !buildTurretsHandles.isDefinedAt(elem._1)).foreach({ case (id, (team, pos)) =>
        buildTurretsHandles += (id -> setTimeout(25000) {
          queueActions(
            List(NewGunTurret(
              GameAction.newId(), Time.getTime, id, teams(team).leader, team, pos, GunTurret.defaultRadius, ServerSource
            ))
          )
          buildTurretsHandles -= id
      })
    })
  }

  def managePlayingState(time: Long): Unit = {
    manageActionChangers(time)
    manageAbilityGivers(time)
    manageDamageZones(time)
    manageHealingZones(time)
    manageBulletAmplifiers(time)
    manageBarriers(time)
    manageSmashBullets(time)
    manageGunTurret(time)
    manageRelevantAbilities(time)
    manageBullets(time)
    manageDeadPlayers(time)
    manageTeamFlags(time)
  }

  def managePreGameState(): Unit = {
    if (gameState.players.isEmpty) {
      // need to create the players, and send these first actions to the players

      val centerSquareRadius: Double = 50.0 * math.sqrt(2)

      val time = Time.getTime

      val i = Complex.i

      val smallHorizontal = Obstacle.segmentObstacleVertices(-50, 50, 10)
      val smallVertical = Obstacle.segmentObstacleVertices(-125 * i, 125 * i, 10)
      val longVertical = Obstacle.segmentObstacleVertices(-500 * i, 500 * i, 10)

      queueActions(
        List(
          gameArea.createCenterSquare(centerSquareRadius, ServerSource),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(gWidth / 2 - teamZoneWidth, -gHeight / 2 + 125), smallVertical, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(gWidth / 2 - teamZoneWidth, gHeight / 2 - 125), smallVertical, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(gWidth / 2 - teamZoneWidth, 0), longVertical, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(gWidth / 2 - 50, -50), smallHorizontal, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(gWidth / 2 - 50, 50), smallHorizontal, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(-gWidth / 2 + teamZoneWidth, -gHeight / 2 + 125), smallVertical, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(-gWidth / 2 + teamZoneWidth, gHeight / 2 - 125), smallVertical, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(-gWidth / 2 + teamZoneWidth, 0), longVertical, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(-gWidth / 2 + 50, -50), smallHorizontal, ServerSource
          ),
          NewObstacle(
            GameAction.newId(), time, Entity.newId(),
            Complex(-gWidth / 2 + 50, 50), smallHorizontal, ServerSource
          )
        ) ++
          gameArea.gameAreaEdgesVertices.map({
            case (pos, vertices) =>
              NewObstacle(GameAction.newId(), Time.getTime + 1, Entity.newId(), pos, vertices, ServerSource)
          }) ++
        (1 to 2).toList.flatMap(_ => {
          val (x, y) = gameArea.randomPos(0, gWidth - 2 * teamZoneWidth - 200, gHeight - 200)
          val width = scala.util.Random.nextInt(50) + 50
          val height = scala.util.Random.nextInt(50) + 50

          val vertices = Vector(
            Complex(-width / 2, -height / 2), Complex(width / 2, -height / 2),
            Complex(width / 2, height / 2), Complex(-width / 2, height / 2)
          )

          // we put obstacles in a symmetric way to keep the game symmetric
          List(
            NewObstacle(GameAction.newId(), time, Entity.newId(), Complex(x, y), vertices, ServerSource),
            NewObstacle(GameAction.newId(), time, Entity.newId(), Complex(-x, -y), vertices, ServerSource)
          )
        }) ++
          teams.values.toList.flatMap(team => {
            val pos: Complex = popPositions(team.teamNbr)
            team.playerIds.map(id => {
              val info = playersInfo.find(_.id == id).get
              val newPlayer = new Player(
                info.id, info.team, time, info.playerName, pos.re, pos.im,
                allowedAbilities = info.abilities, relevantUsedAbilities = Map()
              )

              NewPlayerAction(GameAction.newId(), newPlayer, Time.getTime, ServerSource)
            })
          }) ++
          turretIds.map({ case (id, (team, pos)) =>
            NewGunTurret(
              GameAction.newId(), time, id, teams(team).leader, team, pos, GunTurret.defaultRadius, ServerSource
            )
          }) ++
          flagZones.map({ case (teamNbr, zone) =>
              NewTeamFlag(GameAction.newId(), time, Entity.newId(), teamNbr, zone.pos, ServerSource)
          })
      )

      broadcastActions()
      broadcastReliable(GameStartsIn(gameName, 3000))

      setTimeout(3000) {
        performAction()
      }
    } else {

      queueActions(List(GameBegins(GameAction.newId(), Time.getTime, new ConvexPolygon(Vector(
        Complex(-gameArea.width / 2, -gameArea.height / 2),
        Complex(gameArea.width / 2, -gameArea.height / 2),
        Complex(gameArea.width / 2, gameArea.height / 2),
        Complex(-gameArea.width / 2, gameArea.height / 2)
      )), ServerSource)))


      broadcastActions()
      _state = Playing

      setIntervalHandlers +:= setInterval(1000 / 10) {
        broadcastActions()
      }

      setTimeout(2000) {
        addZone(gameState)
        setIntervalHandlers +:= setInterval(DamageZone.popRate) {
          addZone(gameState)
        }
      }

      setTimeout(1000 / 60) {
        performAction()
      }
    }


  }

  def manageGamePlayingEnded(): Unit = {
    setTimeoutHandles.foreach(scala.scalajs.js.timers.clearTimeout)
  }


  override def addZone(gameState: GameState): Unit = {
    findPosition(0, gWidth - 2 * teamZoneWidth - DamageZone.maxRadius, gHeight, 10000, 0) match {
      case Some((x, y)) =>
        val time = Time.getTime
        queueActions(List(
          UpdateDamageZone(
            GameAction.newId(), time, Entity.newId(), time, time, x, y, DamageZone.startingRadius, ServerSource
          )
        ))
      case _ =>
    }
  }

}
