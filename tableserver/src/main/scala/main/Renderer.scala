package main

import exceptions._
import networkcom.messages.SendPlayerInfo
import networkcom.tablemessages._
import networkcom.{Disconnect, Message, Peer, Server}
import tables.Table

object Renderer {

  def main(args: Array[String]): Unit = {

    println("Table server opened")

    val server = new Server {
      override val address: String = "*"

      override val port: Int = 22222

      override def messageCallback(server: Server, peer: Peer, msg: Message): Unit = {

        def answer(message: Message): Unit = sendOrderedReliable(message, peer)

        msg match {
          case msg: TableServerMessages =>
            msg match {
              case OpenTable(playerName, name, gameMode, password) =>
                def error(msg: String): Unit =
                  answer(TableOpened(name, success = false, msg))

                if (Table.playerAlreadyConnected(peer)) {
                  error("You're already seated at a table.")
                } else {
                  try {
                    val t = Table(playerName, name, gameMode, if (password == "") None else Some(password), this)
                      .newPlayer(playerName, peer)
                    answer(TableOpened(name, success = true, ""))

                    t.broadcastTableInfo()
                  } catch {
                    case _: TooManyTables =>
                      error("Too many tables already opened.")
                    case _: TableNameAlreadyUsed =>
                      error(s"Table name $name already used.")
                    case _: NoSuchModeException =>
                      error(s"Game mode $gameMode does not exist.")
                    case _: EmptyPlayerNameException =>
                      error(s"Player name can't be empty.")
                    case e: TooLongPlayerNameException =>
                      error(e.msg)
                    case _: Throwable =>
                      println("Weird stuff happened when receiving OpenTable message.")
                      error("Weird stuff happened when receiving OpenTable message.")
                  }
                }

              case JoinTable(playerName, tableName, password) =>
                def error(msg: String): Unit =
                  answer(TableJoined(playerName, tableName, success = false, msg))

                if (Table.playerAlreadyConnected(peer)) {
                  error("You're already seated at a table.")
                } else {
                  try {
                    val table = Table.table(tableName)
                    if (!table.started && (!table.needPassword || table.password.get == password)) {
                      table.newPlayer(playerName, peer)
                      answer(TableJoined(playerName, tableName, success = true, ""))
                      table.broadcastTableInfo()
                    } else {
                      error("Invalid password")
                    }
                  } catch {
                    case e: TooLongPlayerNameException =>
                      error(e.msg)
                    case _: TableDoesNotExist =>
                      error(s"Table $tableName does not exist.")
                    case _: PlayerAlreadySeated => // should never happen as it is already checked before
                      error("You're already seated at this table.")
                  }
                }

              case LeaveTable(tableName) =>
                Table.removePeerFromExistence(peer)
                if (Table.doesTableExist(tableName)) {
                  Table.table(tableName).broadcastTableInfo()
                }

              case LaunchGameFromTable(tableName, _, _) =>
                def error(msg: String): Unit =
                  answer(LaunchGameFromTable(tableName, success = false, msg))

                try {
                  Table.table(tableName).launchGame(peer, this)
                } catch {
                  case _: TableDoesNotExist =>
                    error("Table does not exist.")
                  case _: NotHostTryToLaunch =>
                    error("Only the host may launch.")
                  case e: TooManyPlayers =>
                    error(s"Maximum number of player is currently ${e.maxNumber}.")
                  case e: CanNotLaunchGame =>
                    error(e.errorMessage)
                  case _: Throwable =>
                    println("Weird stuff happened in LaunchGameFromTable callback")
                    error("Weird stuff happened in LaunchGameFromTable callback")
                }

              case HolePunching(tableName) =>
                try {
                  Table.table(tableName).sendServerInfo(peer, this)
                } catch {
                  case _: Throwable =>
                    println("Weird stuff happened in HolePunching callback")
                }

              case AskTableInfo(tableName, playerAsking) =>
                try {
                  Table.table(tableName).sendTableInfo(playerAsking, this)
                } catch {
                  case _: Throwable =>
                }

              case AskTables() =>
                answer(Table.tablesMessage)

              case Hello(tableName) =>
                Table.table(tableName).sendPlayerClientInfoToServer(peer)
            }

          case SendPlayerInfo(gameName, playerName, _, team, ready, abilities, _) =>
            try {
              Table.table(gameName).receivedPlayerInfo(
                playerName, ready, abilities, team
              )
            } catch {
              case _: Throwable =>
                println(s"Trying to add info to table $gameName that does not exist. Weird.")
            }
          case Disconnect() =>
            Table.removePeerFromExistence(peer)
            removeConnection(peer)
          case _ =>
            println(msg)
        }



      }

      override def clientConnectedCallback(server: Server, peer: Peer, connected: Boolean): Unit = {

        if (connected) {
          println(s"a client connected (peer $peer)")
        } else {
          println(s"a client disconnected (peer $peer)")
          Table.removePeerFromExistence(peer)
        }

      }
    }

    server.activate()

    scala.scalajs.js.timers.setInterval(30000) {
      println("Server alive")
      println(s"Number of connected clients: ${server.clients.size}")
    }

  }

}
