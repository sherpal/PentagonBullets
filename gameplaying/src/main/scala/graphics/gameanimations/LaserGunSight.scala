package graphics.gameanimations

import gamestate.GameState
import graphics.{Camera, GameAnimation}
import pixigraphics.{PIXIContainer, PIXIGraphics}

class LaserGunSight(playerId: Long, stage: PIXIContainer) extends GameAnimation {

  private val graphics: PIXIGraphics = new PIXIGraphics()
    .beginFill(0xFF0000)
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
          .beginFill(0xFF0000)
          .lineStyle(2, 0xFF0000)
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

