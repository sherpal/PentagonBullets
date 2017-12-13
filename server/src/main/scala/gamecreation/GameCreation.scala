package gamecreation

import abilities.Ability
import entities.Entity
import exceptions.GameAlreadyStarted
import gamelogicserver.GamePlaying
import gamemode.GameMode
import gameserver.GameServer
import networkcom.messages.SendPlayerInfo
import networkcom.{Peer, PlayerGameSettingsInfo}

import scala.collection.mutable

trait GameCreation {

  val name: String

  val gameMode: GameMode

  val hostName: String

  protected val hostPeer: Peer

  protected val server: GameServer

  val id: Long = GameCreation.getId

  private val players: mutable.Map[String, Peer] = mutable.Map()

  private val _playersInfo: mutable.Map[String, PlayerGameSettingsInfo] = mutable.Map()
  private def playersInfo: Map[String, PlayerGameSettingsInfo] = _playersInfo.toMap


  def containsPlayer(player: String): Boolean = players.keys.toList.contains(player)

  def currentPlayers: Array[String] = players.keys.toArray

  def currentPlayersInfo: Array[SendPlayerInfo] = playersInfo.values.toArray.map(_.toSendPlayerInfo(name))

  def currentPlayersWithPeers: Map[String, Peer] = players.toMap

  def addNewPlayer(playerName: String, peer: Peer, id: Int): Boolean = {
    if (gameStarted) throw GameAlreadyStarted()
    else if (bookedPlayerNames.isDefinedAt(id) && bookedPlayerNames(id) == playerName) {
      players += (playerName -> peer)

      def findFreeTeam(from: Int = 1): Int = {
        _playersInfo.values.find(_.team == from) match {
          case Some(_) => findFreeTeam(from + 1)
          case None => from
        }
      }

      val team = findFreeTeam()

      _playersInfo += (playerName -> new PlayerGameSettingsInfo(playerName))
      _playersInfo(playerName).team = team
      true
    } else
      false
  }

  def removePlayer(player: String): Boolean = {
    if (gameStarted) throw GameAlreadyStarted()
    else {
      playersInfo.get(player) match {
        case Some(p) =>
          _playersInfo -= p.playerName
        case _ =>
      }
      players -= player
      bookedPlayerNames.clone.find(elem => elem._2 == player) match {
        case Some((reservationID, _)) =>
          bookedPlayerNames -= reservationID
        case None =>
      }
      player == hostName
    }
  }

  def setPlayerReadyStatus(playerName: String, status: Boolean): Unit = {
    _playersInfo.get(playerName) match {
      case Some(player) =>
        player.ready = status
      case None =>
    }
  }

  def setPlayerTeam(playerName: String, team: Int): Unit = {
    _playersInfo.get(playerName) match {
      case Some(player) =>
        player.team = team
      case None =>
    }
  }

  def addPlayerAbility(playerName: String, abilityId: Int): Unit = {
    _playersInfo.get(playerName) match {
      case Some(player) =>
        player.abilities = player.abilities.filterNot(Ability.playerChoices.contains) :+ abilityId
      case None =>
    }
  }

  def removePlayerAbility(playerName: String, abilityId: Int): Unit = {
    _playersInfo.get(playerName) match {
      case Some(player) =>
        player.abilities = player.abilities.filter(_ != abilityId)
      case None =>
    }
  }

  private val bookedPlayerNames: mutable.Map[Int, String] = mutable.Map(0 -> hostName)
  def getBookedPlayerNames: Set[(Int, String)] = bookedPlayerNames.toSet

  def bookName(name: String, id: Int): Option[String] = {
    if (bookedPlayerNames.values.toList.contains(name)) Some("Name already used.")
    else if (hasStarted) Some("Game has already started.")
    else {
      bookedPlayerNames += (id -> name)
      None
    }
  }

  def unBookName(id: Int): Unit =
    bookedPlayerNames -= id

  private var gameStarted: Boolean = false
  def hasStarted: Boolean = gameStarted

  def launchGame(password: Int, sendPlayerInfo: Array[SendPlayerInfo]): GamePlaying = {
    gameStarted = true
    val playerInfo = sendPlayerInfo.toVector.map(info => {
      val player = new PlayerGameSettingsInfo(info.playerName)
      player.team = info.team
      // every player has the activate shield ability
      player.abilities :+= Ability.activateShieldId

      info.abilities.foreach(player.abilities :+= _)

      player.ready = true
      player.id = info.id

      player
    })
    makeGamePlaying(password, playerInfo)
  }

  def makeGamePlaying(password: Int, playerInfo: Vector[PlayerGameSettingsInfo]): GamePlaying

  def sendPlayerInfoArray: Array[SendPlayerInfo] = playersInfo.values.map(info =>
    SendPlayerInfo(name, info.playerName, Entity.newId(), info.team, info.ready, info.abilities)
  ).toArray
}

object GameCreation {
  private val idQueue: mutable.Queue[Long] = mutable.Queue()

  private var lastId: Long = 0

  private def getId: Long = if (idQueue.isEmpty) {
    lastId += 1
    lastId
  } else {
    idQueue.dequeue()
  }

  def freeId(id: Long): Unit =
    idQueue.enqueue(id)

}