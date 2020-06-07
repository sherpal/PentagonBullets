package gameinfo

import communication.PlayerClient
import custommath.Complex
import gamestate.ActionSource.PlayerSource
import gamestate.{GameAction, GameState}
import gamestate.actions._
import globalvariables._
import gui.{Frame, ScriptKind}

import scala.collection.mutable

case class GameStatistics(
    id: Long,
    playerName: String,
    teamId: Int,
    ability: String,
    pos: Complex,
    sentBullets: List[Long],
    sentBulletsTimes: List[Long],
    bulletHits: Int,
    bulletHitsTimes: List[Long],
    damageTaken: Double,
    bulletHitPlayerNbr: Int,
    takenHealUnits: Int,
    totalMovement: Double,
    deathTime: Option[Long]
) {

  private def insertLong(long: Long, list: List[Long]): List[Long] =
    if (list.isEmpty) List(long)
    else if (list.head < long) long +: list
    else list.head +: insertLong(long, list.tail)

  private def didSentBullet(id: Long): Boolean = {
    def isInThereAcc(id: Long, list: List[Long]): Boolean =
      if (list.isEmpty) false
      else if (list.head < id) false
      else if (list.head == id) true
      else isInThereAcc(id, list.tail)

    isInThereAcc(id, sentBullets)
  }

  def sentBulletsNbr: Int = sentBullets.length

  def sendBullet(id: Long, time: Long): GameStatistics = copy(
    sentBullets      = insertLong(id, sentBullets),
    sentBulletsTimes = insertLong(time, sentBulletsTimes)
  )

  def takeDamages(amount: Double): GameStatistics = copy(damageTaken = damageTaken + amount)

  def takeBullets(times: List[Long]): GameStatistics = copy(
    bulletHits      = bulletHits + times.length,
    bulletHitsTimes = times.foldLeft(bulletHitsTimes) { case (ts, t) => insertLong(t, ts) }
  )

  def hitPlayer(nbr: Int = 1): GameStatistics = copy(bulletHitPlayerNbr = bulletHitPlayerNbr + nbr)

  def takeHealUnit(): GameStatistics = copy(takenHealUnits = takenHealUnits + 1)

  def move(newPos: Complex): GameStatistics = copy(pos = newPos, totalMovement = totalMovement + !(newPos - pos))

  def setPos(newPos: Complex): GameStatistics = copy(pos = newPos)

  def toPlayerStat(lifeOverTime: List[(Long, Double)]): PlayerStat = {
    val (red, green, blue) = PlayerClient.playerClient.gameHandler.playerColors(id)
    PlayerStat(
      id,
      playerName,
      Color(red, green, blue),
      teamId,
      ability,
      sentBulletsTimes,
      sentBullets,
      bulletHits,
      bulletHitsTimes,
      damageTaken,
      bulletHitPlayerNbr,
      takenHealUnits,
      totalMovement,
      deathTime,
      lifeOverTime.map({ case (time, life) => LifeTimeStamp(time, life) })
    )
  }

}

object GameStatistics {

  private val playerStats: mutable.Map[Long, GameStatistics] = mutable.Map()

  private val playerLivesOverTime: mutable.Map[Long, List[(Long, Double)]] = mutable.Map()

  def newPlayer(id: Long, playerName: String, teamId: Int, ability: String): Unit = {
    playerStats += id -> GameStatistics(
      id,
      playerName,
      teamId,
      ability,
      Complex(0, 0),
      Nil,
      Nil,
      0,
      Nil,
      0,
      0,
      0,
      0,
      None
    )
    playerLivesOverTime += id -> Nil
  }

  private def applyAction(action: GameAction, gameState: GameState): Unit =
    gameState
      .applyActionChangers(action)
      .foreach({
        case NewBullet(_, id, playerId, _, _, _, _, _, time, _, source) if source == PlayerSource =>
          playerStats += playerId -> playerStats(playerId).sendBullet(id, time)
        case UpdatePlayerPos(_, _, playerId, x, y, _, _, _, source) if source == PlayerSource =>
          playerStats += playerId -> playerStats(playerId).move(Complex(x, y))
        case PlayerHitByMultipleBullets(_, time, ids, playerId, damage, _) =>
          playerStats += playerId ->
            playerStats(playerId).takeDamages(damage).takeBullets((1 to ids.length).map(_ => time).toList)
          ids
            .groupBy(id => playerStats.find(_._2.didSentBullet(id)))
            .filter(_._1.isDefined)
            .map(elem => (elem._1.get._1, elem._2.length))
            .foreach({
              case (id, bulletNbr) =>
                playerStats += id -> playerStats(id).hitPlayer(bulletNbr)
            })
          updatePlayerLife(playerId, gameState)
        case PlayerTakeDamage(_, _, plrId, _, damage, _) =>
          playerStats += plrId -> playerStats(plrId).takeDamages(damage)
          updatePlayerLife(plrId, gameState)
        case PlayerTakeHealUnit(_, _, playerId, _, _) =>
          playerStats += playerId -> playerStats(playerId).takeHealUnit()
          updatePlayerLife(playerId, gameState)
        case HealingZoneHeals(_, _, plrId, _, _, _) =>
          updatePlayerLife(plrId, gameState)
        case NewPlayer(_, player, _, _) =>
          playerStats += player.id -> playerStats(player.id).setPos(player.pos)
        case PlayerDead(_, time, playerId, _, _) =>
          playerStats += playerId -> playerStats(playerId).copy(deathTime = Some(time))
          updatePlayerLives(gameState)
          aPlayerDied(playerId, gameState)
        case PlayerHitBySmashBullet(_, _, playerId, _, _) =>
          updatePlayerLife(playerId, gameState)
        case GameBegins(_, _, _, _) =>
          updatePlayerLives(gameState)
          frame.setScript(ScriptKind.OnUpdate)((dt: Double) => {
            timeSinceLastUpdate += dt

            if (timeSinceLastUpdate > playerLivesUpdateRate) {
              timeSinceLastUpdate -= playerLivesUpdateRate

              updatePlayerLives(currentGameState)
            }
          })
        case _ =>
      })

  def saveGameStatistics(playerName: String, startTime: Long, finalGameState: GameState): Unit = DataStorage.storeValue(
    "statistics",
    PlayerStats(
      playerName,
      finalGameState.time - startTime,
      startTime,
      playerStats.values.toList
        .map(playerStat => playerStat.toPlayerStat(playerLivesOverTime(playerStat.id)))
        .sortWith({
          case (p1, p2) =>
            if (p1.deathTime.isEmpty) true
            else if (p2.deathTime.isEmpty) false
            else p1.deathTime.get > p2.deathTime.get
        })
    )
  )

  private val frame: Frame = new Frame()
  GameEvents.registerAllEvents(frame, (action, gameState) => applyAction(action, gameState))

  private def currentGameState: GameState = PlayerClient.playerClient.gameHandler.currentGameState

  private def updatePlayerLives(gameState: GameState): Unit =
    gameState.players.values.foreach(player => {
      playerLivesOverTime += player.id ->
        ((gameState.time, player.lifeTotal) +: playerLivesOverTime(player.id))
    })

  private def aPlayerDied(plrId: Long, gameState: GameState): Unit =
    playerLivesOverTime += plrId -> ((gameState.time, 0.0) +: playerLivesOverTime(plrId))

  private def updatePlayerLife(plrId: Long, gameState: GameState): Unit =
    gameState.players.get(plrId) match {
      case Some(player) =>
        playerLivesOverTime += plrId -> ((gameState.time, player.lifeTotal) +: playerLivesOverTime(plrId))
      case _ =>
    }

  private val playerLivesUpdateRate: Double = 500
  private var timeSinceLastUpdate: Double   = playerLivesUpdateRate

}
