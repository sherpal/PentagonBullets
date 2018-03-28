package tables

import exceptions._
import gamemode.{CaptureTheFlagMode, GameMode, StandardMode}
import networkcom.{Message, Peer, Server}
import networkcom.tablemessages.{CreateClient, CreateServer, PlayerInfoMessage, PlayerPeers, TableDestroyed, Tables, Table => TableMessage}
import org.scalajs.dom.ext.Color

import scala.collection.mutable

trait Table {

  val host: String // player that opened the table. The only one that can launch the game.
  val name: String
  val gameMode: GameMode
  val password: Option[String]
  val server: Server

  private var gameHasStarted: Boolean = false

  def started: Boolean = gameHasStarted

  /** Simply records the player names and peers. */
  private val players: mutable.Map[String, Peer] = mutable.Map()

  private def playersFromPeer: Map[Peer, String] = players.toMap.map(_.swap)

  type T <: Table.PlayerInfo

  protected val playersInfo: mutable.Map[String, T]

  def receivedPlayerInfo(playerName: String, ready: Boolean, abilities: List[Int], team: Int): Unit

  def newInfo(playerName: String): T

  /** Check if a player is already seated. */
  protected def isPlayerSeated(playerName: String): Boolean = players.isDefinedAt(playerName)

  private def isPlayerSeated(peer: Peer): Boolean = playersFromPeer.isDefinedAt(peer)

  /** Adds a player to the table. If the player is already seated, throws a PlayerAlreadySeated exception. */
  def newPlayer(playerName: String, peer: Peer): Table = {
    if (isPlayerSeated(playerName)) {
      throw new PlayerAlreadySeated
    } else if (playerName.length > 12) {
      throw new TooLongPlayerNameException("The maximum number of characters for player names is 12.")
    }
    players += playerName -> peer
    playersInfo += playerName -> newInfo(playerName)

    this
  }

  def removePlayer(playerName: String): Table = {
    players -= playerName
    playersInfo -= playerName

    if (players.isEmpty || host == playerName) {
      Table.tables -= this

      broadcastToPlayers(TableDestroyed(tableName = name), server)
    }

    this
  }

  def removePlayer(peer: Peer): Table = {
    if (isPlayerSeated(peer)) {
      removePlayer(playersFromPeer(peer))
    } else this
  }

  /** Sends a message to every player seated at this table through the given server. */
  def broadcastToPlayers(message: Message, server: Server): Unit = {
    players.keys.foreach(sendToPlayer(_, message, server))
  }

  def sendToPlayer(playerName: String, message: Message, server: Server): Unit = {
    server.sendOrderedReliable(message, players(playerName))
  }

  /**
   * Launches the game for the players.
   *
   * @param peer   the peer from which the launch request comes. If peer != players(host), throws an exception.
   * @param server the server where the
   */
  def launchGame(peer: Peer, server: Server): Unit = {
    if (players.getOrElse(host, Peer("", 0)) != peer) {
      throw new NotHostTryToLaunch
    }

    // TODO: check if the game can actually be launched

    if (players.size > Table.definedColors.length) {
      throw new TooManyPlayers(Table.definedColors.length)
    } else if (players.size < 2) {
      throw new CanNotLaunchGame("You can't play alone.")
    }

    gameHasStarted = true

    sendTableInfo(host, server)
    server.sendReliable(CreateServer(), players(host))
  }


  private var oneTimeServerPeer: Option[Peer] = None

  def sendServerInfo(peer: Peer, server: Server): Unit = {
    println(s"The server was created at peer $peer")

    oneTimeServerPeer = Some(peer)

    players.keys.foreach(sendTableInfo(_, server))
    broadcastToPlayers(CreateClient(peer), server)
  }

  def sendPlayerClientInfoToServer(peer: Peer): Unit = {
    server.sendReliable(PlayerPeers(List(peer.address), List(peer.port)), oneTimeServerPeer.get)
  }

  private var createdClients: Int = 0

  def clientWasCreated(): Unit = {
    createdClients += 1
    if (createdClients == playersInfo.size) {
      Table -= this
    }
  }



  /**
   * Sends all the information of the table available to the given player.
   *
   * For example, we do not send the ability chosen by a player to players not on their team.
   *
   * @param playerName the name of the player to give the information to.
   */
  def sendTableInfo(playerName: String, server: Server): Unit

  def broadcastTableInfo(): Unit = {
    players.keys.foreach(sendTableInfo(_, server))
  }

  def toTableMessage: TableMessage = TableMessage(
    name, gameMode.toString, players.keys.toList, password.getOrElse("")
  )

  def needPassword: Boolean = password.isDefined

  def receivedPlayerInfo(message: PlayerInfoMessage): Unit

}


object Table {

  private val tables: mutable.Set[Table] = mutable.Set()

  val maxTableNumber: Int = 1000

  def apply(hostName: String, name: String, gameMode: GameMode, password: Option[String], server: Server): Table = {
    if (tables.exists(_.name == name)) {
      throw new TableNameAlreadyUsed(name)
    } else if (tables.size >= maxTableNumber) {
      throw new TooManyTables
    } else if (hostName == "") {
      throw new EmptyPlayerNameException
    }

    val table = gameMode match {
      case StandardMode =>
        StandardTable(hostName, name, password, server)
      case CaptureTheFlagMode =>
        CaptureTheFlagTable(hostName, name, password, server)
      case _ =>
        throw new NoSuchModeException(s"Game Mode $gameMode is not yet implemented.")
    }

    tables += table
    table
  }

  def table(tableName: String): Table = tables.find(_.name == tableName) match {
    case Some(table) =>
      table
    case None =>
      throw new TableDoesNotExist
  }

  def doesTableExist(tableName: String): Boolean = tables.exists(_.name == tableName)

  def -=(name: String): Unit =
    tables.find(_.name == name) match {
      case Some(table) =>
        tables -= table
      case None =>
    }

  def -=(table: Table): Unit =
    tables -= table



  def tablesMessage: Tables = Tables(tables.toList.filterNot(_.gameHasStarted).map(_.toTableMessage))

  def allConnectedPlayerPeers: Set[Peer] = tables.flatMap(_.players.values).toSet

  def playerAlreadyConnected(peer: Peer): Boolean = allConnectedPlayerPeers.contains(peer)


  def removePeerFromExistence(peer: Peer): Unit = {
    tables.foreach(_.removePlayer(peer))

    if (scala.scalajs.LinkingInfo.developmentMode) {
      println(s"Number of tables: ${tables.size}")
    }
  }


  /**
   * Stores the information of the players at the table.
   * The information of a player depend on the game mode.
   */
  trait PlayerInfo

  val definedColors: List[Color] = List(
    Color(255, 0, 0),
    Color(100, 100, 255),
    Color(0, 255, 0),
    Color(204, 204, 0),
    Color(204, 0, 204),
    Color(102, 205, 170),
    Color(255, 165, 0)
  )

}