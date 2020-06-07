package beforejoining

import communication.TableClient
import gamemode.GameMode
import joined.GameSettings
import networkcom.tablemessages.{JoinTable, TableJoined}
import org.scalajs.dom
import org.scalajs.dom.html
import popups.Alert

import scala.collection.mutable

final class TableInfo(
    val tableName: String,
    val password: Option[String],
    val gameMode: GameMode,
    private var _playerNumber: Int
) {

  def playerNumber: Int = _playerNumber

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]

  div.addEventListener("click", (_: dom.Event) => {
    TableInfo.selectTable(this)
  })

  private val tableNameLabel: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
  tableNameLabel.style.marginRight = "20px"
  tableNameLabel.textContent       = tableName

  private val gameModeLabel: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
  tableNameLabel.style.marginRight = "20px"
  gameModeLabel.textContent        = gameMode.toString

  private val nbrPlayersLabel: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]

  div.appendChild(tableNameLabel)
  div.appendChild(gameModeLabel)
  div.appendChild(nbrPlayersLabel)

  private def setTextContent(): Unit =
    nbrPlayersLabel.textContent = playerNumber.toString

  def changePlayerNumber(nbr: Int): Unit = {
    _playerNumber = nbr
    setTextContent()
  }

  TableInfo.addTable(this)

}

object TableInfo {

  private val playerNameInput: html.Input = dom.document.getElementById("playerNameInput").asInstanceOf[html.Input]
  private def playerName: String          = playerNameInput.value

  private val joinTableButton: html.Button = dom.document.getElementById("joinTable").asInstanceOf[html.Button]

  val tableListDiv: html.Div = dom.document.getElementById("tableListDiv").asInstanceOf[html.Div]

  private var selectedTable: Option[TableInfo] = None

  def selectTable(tableInfo: TableInfo): Unit = {

    tables.foreach(_.div.className = "")

    tableInfo.div.className = "selectedTable"

    selectedTable = Some(tableInfo)

    joinTableButton.disabled = false
  }

  joinTableButton.addEventListener[dom.MouseEvent](
    "click",
    (_: dom.MouseEvent) => {

      List(
        selectedTable.isEmpty -> (() => "You need to select a table."),
        (playerName == "") -> (() => "You need to chose a player name.")
      ).find(_._1) match {
        case Some((_, errorMessage)) =>
          Alert.showAlert("Failed to joint table", errorMessage())
        case None =>
          TableClient.sendOrderedReliable(
            JoinTable(playerNameInput.value, selectedTable.get.tableName, "")
          )
      }
    }
  )

  def receivedTableJoined(msg: TableJoined): Unit =
    if (msg.success) {
      GameSettings.joinTable(
        msg.tableName,
        msg.playerName,
        nbrOfPlayer = numberOfPlayersIn(msg.tableName)
      )
    } else {
      Alert.showAlert("Failed to join table", msg.error)
    }

  private val tables: mutable.Set[TableInfo] = mutable.Set()

  private def addTable(tableInfo: TableInfo): Unit = {
    tables += tableInfo

    showTables()
  }

  private def showTables(): Unit = {
    (0 until tableListDiv.children.length).foreach(_ => tableListDiv.removeChild(tableListDiv.lastChild))

    tables.toList.sortBy(_.tableName).foreach(table => tableListDiv.appendChild(table.div))
  }

  def updateTables(list: List[networkcom.tablemessages.Table]): Unit = {

    tables.toSet
      .filter(tableInfo => {
        list.find(_.name == tableInfo.tableName) match {
          case Some(t) => GameMode.fromString(t.gameMode) != tableInfo.gameMode // same name but different game mode
          case None    => true // does not exist anymore
        }
      })
      .foreach(tables -= _)

    list.map(
      table =>
        tables.find(_.tableName == table.name) match {
          case Some(t) =>
            t.changePlayerNumber(table.playerNames.length)
            t
          case None =>
            new TableInfo(
              table.name,
              if (table.password == "") None else Some(table.password),
              table.gameMode,
              table.playerNames.length
            )
        }
    )

    showTables()
  }

  def doesTableExist(tableName: String): Boolean = tables.exists(_.tableName == tableName)

  private def numberOfPlayersIn(tableName: String): Int =
    tables.find(_.tableName == tableName) match {
      case Some(table) => table.playerNumber
      case None        => 0
    }

}
