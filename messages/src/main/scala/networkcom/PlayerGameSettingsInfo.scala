package networkcom

import networkcom.messages.SendPlayerInfo

/**
 * Gather information about a player hosting or joining a game before launch.
 */
final class PlayerGameSettingsInfo(val playerName: String) {

  var ready: Boolean = false

  var abilities: List[Int] = List()

  var team: Int = 1

  var id: Long = 0: Long

  var color: (Double, Double, Double) = (0,0,0)

  def toSendPlayerInfo(gameName: String): SendPlayerInfo = SendPlayerInfo(
    gameName, playerName, id, team, ready, abilities, Vector(color._1, color._2, color._3)
  )

  override def toString: String = List[String](
    ready.toString, team.toString, id.toString, abilities.mkString(",")
  ).mkString(";")

}

object PlayerGameSettingsInfo {

  def fromString(playerName: String, info: String): PlayerGameSettingsInfo = try {
    val infoList = info.split(";")
    val ready = infoList.head.toBoolean
    val team = infoList.tail.head.toInt
    val id = infoList.tail.tail.head.toLong
    val abilities = infoList.tail.tail.tail.head.split(",").map(_.toInt).toList
    val colors = infoList.tail.tail.tail.tail.head.split(",").map(_.toDouble).toVector
    fromInfo(playerName, ready, team, id, abilities, (colors(0), colors(1), colors(2)))
  } catch {
    case e: Throwable =>
      println("There was a problem reading a PlayerGameSettingsInfo string.")
      throw e
  }

  def fromInfo(
                playerName: String,
                ready: Boolean,
                team: Int,
                id: Long,
                abilities: List[Int],
                color: (Double, Double, Double)
              ): PlayerGameSettingsInfo =
  {
    val info = new PlayerGameSettingsInfo(playerName)
    info.ready = ready
    info.team = team
    info.id = id
    info.abilities = abilities
    info.color = color
    info
  }

}
