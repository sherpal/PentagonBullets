package gamegui

import communication.PlayerClient
import entities.Player
import gamemode.{CaptureTheFlagMode, StandardMode}
import gamestate.GameState
import gui._
import pixigraphics.PIXITexture

/**
  * ScoreBoard lists players from first to last in terms of points.
  */
object ScoreBoard extends Tooltip() {

  setPoint(TopRight)
  setSize(200, 10)

  private val background = createSprite()
  background.setAllPoints()
  background.setVertexColor(198 / 255.0, 195 / 255.0, 214 / 255.0, 0.5)

  var colorMap: Map[String, (Double, Double, Double)] = Map()

  private var lastUpdate: Double = 0

  def update(gameState: GameState): Unit = {
    //if (gameState.time - lastUpdate > 1000) {
    lastUpdate = gameState.time
    topLife match {
      case Some(bar) =>
        bar.foreach(life => {
          life
            .asInstanceOf[PlayerLife]
            .setLife(
              gameState.players.get(life.asInstanceOf[PlayerLife].playerId) match {
                case Some(player) =>
                  player.lifeTotal
                case None =>
                  0.0
              }
            )
        })
      case None =>
    }
    //}
  }

  def reset(time: Long): Unit = {
    lastUpdate = time
    topLife match {
      case Some(bar) =>
        bar.foreach(_.asInstanceOf[PlayerLife].setLife(0.0))
      case None =>
    }
  }

  setScript(ScriptKind.OnUIParentResize)(() => {
    clearAllPoints()
    PlayerClient.playerClient.gameMode match {
      case StandardMode =>
        setPoint(TopRight)
      case CaptureTheFlagMode =>
        setPoint(BottomLeft)
    }
  })

  /**
    * Attempts to use the StackedFrame
    */
  private class PlayerLife(val playerName: String, val playerId: Long, color: (Double, Double, Double))
      extends Frame
      with StackedFrame {
    setSize(200, 25)

    private lazy val bar: StatusBar = {
      val b = new StatusBar(this)
      b.setSize(175, 25)
      b.setStatusBarTexture()
      b.setMinMaxValues(0, Player.maxLife)
      b.setValue(Player.maxLife)
      b.setPoint(Right)
      b.setStatusBarColor(0, 1, 0, 0.7)
      b
    }

    def initialize(): Unit = {
      val itemBullet: GUISprite = createSprite()
      itemBullet.setSize(25)
      itemBullet.setPoint(Left)
      itemBullet.setTexture(PIXITexture.fromImage("../../assets/ui/player_item_bullet.png"))
      itemBullet.setVertexColor(color._1, color._2, color._3)

      val bitmapText = bar.createBitmapText(playerName, "25px QuicksandBold", "left")
      bitmapText.setPoint(Left, bar, Left, 5, 0)
    }

    def setLife(amount: Double): Unit = {
      bar.setValue(amount)
      if (amount / Player.maxLife <= 0.2) {
        bar.setStatusBarColor(1, 0, 0, 0.7)
      } else if (amount / Player.maxLife <= 0.5) {
        bar.setStatusBarColor(1, 163 / 255.0, 0, 0.7)
      } else {
        bar.setStatusBarColor(0, 1, 0, 0.7)
      }
    }

  }

  private var topLife: Option[PlayerLife] = None

  def addPlayerLife(playerName: String, playerId: Long, color: (Double, Double, Double)): Unit = {
    topLife match {
      case Some(top) =>
        top.addChild(new PlayerLife(playerName, playerId, color))
      case None =>
        topLife = Some(new PlayerLife(playerName, playerId, color))
        topLife.get.setPoint(Top, this, Top)
    }

    topLife.get.last.asInstanceOf[PlayerLife].initialize()

    ScoreBoard.setHeight(topLife.get.length * topLife.get.height)
  }

}
