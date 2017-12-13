package entitiescollections

import custommath.Complex
import entities.{GameArea, Zone}

/**
 *
 */
final class CaptureTheFlagInfo(playerInfo: Seq[CaptureTheFlagInfo.PlayerInfo]) {

  val teams: Map[Int, PlayerTeam] = playerInfo.groupBy(_.team).map(elem =>
    elem._1 -> new PlayerTeam(elem._2.head.team, elem._2.map(_.id))
  )

  val teamsByPlayerId: Map[Long, PlayerTeam] = teams.flatMap(elem => elem._2.playerIds.map(_ -> elem._2))

  val teamNumbers: List[Int] = teams.values.map(_.teamNbr).toList

  val numberOfTeams: Int = teamNumbers.length

  // TODO: change this when more than two teams are allowed.
  val (gWidth, gHeight, teamZoneWidth) = CaptureTheFlagInfo.dimensions(numberOfTeams)

  val gameArea: GameArea = new GameArea(gWidth, gHeight)

  val flagZones: Map[Int, Zone] = teamNumbers.zip(Zone.flagZones(teamNumbers)).toMap

  val popPositions: Map[Int, Complex] = teams.keys.map(teamNbr => {
    teamNbr -> (if (teamNbr == teamNumbers.head) {
      Complex(-gWidth / 2 + 50, -gHeight / 2 + 50)
    } else {
      Complex(gWidth / 2 - 50, gHeight / 2 - 50)
    })
  }).toMap

  val popPositionsByPlayerId: Map[Long, Complex] = playerInfo.map(info => info.id -> popPositions(info.team)).toMap



}

object CaptureTheFlagInfo {

  final case class PlayerInfo(playerName: String, abilities: List[Int], team: Int, id: Long)

  def dimensions(teamNumber: Int): (Int, Int, Int) = (4000, 2000, 500)

  val resurrectionTime: Long = 25000

}