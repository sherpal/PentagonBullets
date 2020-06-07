package entities

import custommath.Complex
import gamestate.GameState
import physics.Circle

class TeamFlag(
    val id: Long,
    val xPos: Double,
    val yPos: Double,
    val teamNbr: Int,
    val bearerId: Option[Long],
    val takenBy: List[Int]
) extends Body {

  val shape: Circle = TeamFlag.flagShape

  val rotation: Double = 0

  def currentPosition(gameState: GameState, time: Long): Complex = bearerId match {
    case Some(playerId) =>
      gameState.players(playerId).currentPosition(time, gameState.obstacles.values)
    case None =>
      pos
  }

  def isBorn: Boolean = bearerId.isDefined

}

object TeamFlag {

  val radius: Int = 20

  val flagShape: Circle = new Circle(radius)

  def scores(gameState: GameState): Map[Int, Int] = {
    val teamsThatTookFlag = gameState.flags.values
      .flatMap(flag => flag.takenBy)

    gameState.flags.values.map(_.teamNbr).map(n => n -> teamsThatTookFlag.count(_ == n)).toMap
  }

}
