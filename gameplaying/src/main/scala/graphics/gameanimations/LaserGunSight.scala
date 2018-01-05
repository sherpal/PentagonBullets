package graphics.gameanimations

import gamestate.GameState
import graphics.{Camera, GameAnimation}
import pixigraphics.{PIXIContainer, PIXIGraphics}
import webglgraphics.Vec3

class LaserGunSight(playerId: Long, stage: PIXIContainer, color: (Double, Double, Double)) extends GameAnimation {

  private val intColor: Int = Vec3(color._1, color._2, color._3).toInt

  private val graphics: PIXIGraphics = new PIXIGraphics()
    .beginFill(intColor)
    .drawCircle(100,100,50)
    .endFill()

  stage.addChild(graphics)

  val duration: Option[Long] = None

  def stopRunningCallback(): Unit = {
    stage.removeChild(graphics)
  }

  protected def animate(gameState: GameState, now: Long, camera: Camera): Unit = {
    (gameState.players.get(playerId), gameState.laserLaunchers.values.find(_.ownerId == playerId)) match {
      case (Some(player), Some(launcher)) =>
        graphics.visible = true
        val playerPos = player.currentPosition(now - player.time)
        val launcherPos = launcher.pos

        val localPlayerPos = camera.worldToLocal(playerPos)
        val localLauncherPos = camera.worldToLocal(launcherPos)

        graphics
          .clear()
          .beginFill(intColor)
          .lineStyle(2, intColor)
          .moveTo(localPlayerPos._1, localPlayerPos._2)
          .lineTo(localLauncherPos._1, localLauncherPos._2)
          .endFill()
      case _ =>
        if (graphics.visible)
          graphics.visible = false
    }
  }

  run()

}

