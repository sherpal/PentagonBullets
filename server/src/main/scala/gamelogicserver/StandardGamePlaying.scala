package gamelogicserver

import custommath.Complex
import entities._
import gamemode.{GameMode, StandardMode}
import gameserver.GameServer
import gamestate.ActionSource.ServerSource
import gamestate.GameAction
import gamestate.actions._
import gamestate.actions.{NewPlayer => NewPlayerAction}
import networkcom.PlayerGameSettingsInfo
import networkcom.messages.GameStartsIn
import physics.ConvexPolygon
import time.Time

import scala.scalajs.js.timers.{setInterval, setTimeout}

class StandardGamePlaying(val gameName: String,
                          val password: Int,
                          protected val playersInfo: Vector[PlayerGameSettingsInfo],
                          protected val server: GameServer) extends GamePlaying {

  val gameMode: GameMode = StandardMode

  val gameArea: GameArea = GameArea(playersInfo.length)

  def isGameOver: Boolean = gameState.players.values.map(_.team).toList.distinct.lengthCompare(1) <= 0

  def managePlayingState(time: Long): Unit = {
    manageActionChangers(time)
    manageHealUnits(time)
    manageAbilityGivers(time)
    manageDamageZones(time)
    manageHealingZones(time)
    manageBulletAmplifiers(time)
    manageBarriers(time)
    manageSmashBullets(time)
    manageGunTurret(time)
    manageMists(time)
    manageRelevantAbilities(time)
    manageBullets(time)
    manageDeadPlayers(time + 1)
  }


  def managePreGameState(): Unit = {
    if (gameState.players.isEmpty) {
      // need to create the players, and send these first actions to the players

      val centerOctagonRadius: Double = 50.0 * math.sqrt(2)

      teams.values.foreach(team => {
        var pos: Option[(Double, Double)] = None
        while (pos.isEmpty) {
          pos = findPosition()

          val z = Complex(pos.get._1, pos.get._2)
          if (z.modulus < centerOctagonRadius + Player.radius) {
            pos = None
          }
        }
        val (x, y) = pos.get
        val startingPos = Complex(x, y)

        queueActions(team.playerIds.map(id => {
          val info = playersInfo.find(_.id == id).get
          val newPlayer = new Player(
            info.id, info.team, Time.getTime, info.playerName, startingPos.re, startingPos.im,
            allowedAbilities = info.abilities, relevantUsedAbilities = Map()
          )

          NewPlayerAction(GameAction.newId(), newPlayer, Time.getTime, ServerSource)
        }))

      })


      queueActions(
        gameArea.createCenterSquare(centerOctagonRadius, ServerSource) +: (
          (1 until 3 * (playersInfo.length - 1)).map(_ => gameArea.createObstacle(gameState, 100, 100, ServerSource)) ++
            gameArea.gameAreaEdgesVertices.map({
              case (pos, vertices) =>
                NewObstacle(GameAction.newId(), Time.getTime + 1, Entity.newId(), pos, vertices, ServerSource)
            })))

      broadcastActions()
      broadcastReliable(GameStartsIn(gameName, 3000))

      setTimeout(3000) {
        performAction()
      }
    } else {

      val time = Time.getTime

      queueActions(List(GameBegins(GameAction.newId(), time, new ConvexPolygon(Vector(
        Complex(-gameArea.width / 2, -gameArea.height / 2),
        Complex(gameArea.width / 2, -gameArea.height / 2),
        Complex(gameArea.width / 2, gameArea.height / 2),
        Complex(-gameArea.width / 2, gameArea.height / 2)
      )), ServerSource)))

      if (playersInfo.length > 2) {
        /** Creating a Mist after two seconds if Players are more than 2 */
        val mistId = Entity.newId()
        setTimeout(500) {
          Mist.setSideShrinkingValue(gameAreaSideLength, playersInfo.length)
          val time = Time.getTime
          // pre creating the mist so that clients can create the textures.
          queueActions(List(
            UpdateMist(
              GameAction.newId(), time, mistId, time, time, gameAreaSideLength - 1, gameAreaSideLength, ServerSource
            )
          ))
        }
        setTimeout(2000) {
          val time = Time.getTime
          queueActions(List(
            UpdateMist(
              GameAction.newId(), time, mistId, time, time, gameAreaSideLength - 1, gameAreaSideLength, ServerSource
            )
          ))
        }
      }

      broadcastActions()
      _state = Playing

      setIntervalHandlers +:= setInterval(1000 / 10) {
        broadcastActions()
      }
      setIntervalHandlers +:= setInterval(5000) {
        addHealUnit()
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


  def manageGamePlayingEnded(): Unit = {}

}
