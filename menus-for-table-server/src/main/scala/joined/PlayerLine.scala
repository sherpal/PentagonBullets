package joined

import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable

/**
  * Represents a player line in the table of players.
  */
final class PlayerLine(val playerName: String) {

  private val div: html.Div = dom.document.createElement("div").asInstanceOf[html.Div]
  div.style.width = "100%"

  private val nameLabel: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
  nameLabel.textContent       = playerName
  nameLabel.style.marginRight = "20px"
  div.appendChild(nameLabel)

  private val teamLabel: html.Label = dom.document.createElement("label").asInstanceOf[html.Label]
  div.appendChild(teamLabel)

  private var _ready: Boolean = false

  def setReady(ready: Boolean): Unit = {
    _ready = ready

    div.className = if (ready) "ready" else ""
  }

  def setTeam(team: Int): Unit =
    teamLabel.textContent = team.toString

  PlayerLine.playerLines += playerName -> this

}

object PlayerLine {

  private val div: html.Div = dom.document.getElementById("players").asInstanceOf[html.Div]

  private val playerLines: mutable.Map[String, PlayerLine] = mutable.Map()

  def playerLine(playerName: String): Option[PlayerLine] = playerLines.get(playerName)

  def showPlayerLines(): Unit = {
    (0 to div.children.length - 2).foreach((_: Int) => div.removeChild(div.lastChild))

    playerLines.toList.sortBy(_._1).map(_._2).map(_.div).foreach(div.appendChild)
  }

  def removePlayerLine(playerName: String): Unit = {
    playerLines -= playerName

    showPlayerLines()
  }

  def removePlayerLinesNotIn(playerNames: List[String]): Unit =
    playerLines.toMap.keys
      .filterNot(playerNames.contains)
      .foreach(playerLines -= _)

}
