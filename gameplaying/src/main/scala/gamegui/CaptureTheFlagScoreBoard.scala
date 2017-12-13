package gamegui


import entities.TeamFlag
import gameinfo.GameEvents
import gamestate.GameState
import gamestate.actions.PlayerBringsFlagBack
import gui.{Tooltip, TopRight}

object CaptureTheFlagScoreBoard extends Tooltip {

  setPoint(TopRight)
  setSize(200, 10)

  private val background = createSprite()
  background.setAllPoints()
  background.setVertexColor(198 / 255.0, 195 / 255.0, 214 / 255.0, 0.5)

  var colorMap: Map[Int, (Double, Double, Double)] = Map()

  def setScores(scores: Map[Int, Int]): Unit = {
    setHeight(scores.size * 25)
    clearLines()

    scores.toList.sortBy(_._2).reverse.foreach({
      case (team, points) =>
        val (r, g, b) = colorMap.getOrElse(team, (24 / 255.0, 77 / 255.0, 30 / 255.0))
        addDoubleLine(
          s"Team $team", points.toString, r, g, b, 20, r, g, b, 20
        )
    })
  }

  registerEvent(GameEvents.OnPlayerBringsBackFlag)((_: PlayerBringsFlagBack, state: GameState) => {
    setScores(TeamFlag.scores(state))
  })




}
