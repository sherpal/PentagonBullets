package entities
import gamestate.{GameAction, GameState}
import gamestate.actions.NewBullet

class BulletGlue(val id: Long, val time: Long, val playerId: Long, val teamId: Int) extends PlayerBuff {

  val duration: Long = 5000

  override def changeAction(action: GameAction): List[GameAction] = action match {
    case NewBullet(
        actionId,
        bulletId,
        plrId,
        team,
        pos,
        radius,
        dir,
        speed,
        bulletTime,
        travelledDistance,
        actionSource
        ) if team != teamId =>
      List(
        NewBullet(
          actionId,
          bulletId,
          plrId,
          team,
          pos,
          radius,
          dir,
          speed / 2,
          bulletTime,
          travelledDistance,
          actionSource
        )
      )
    case _ =>
      List(action)
  }

  def start(gameState: GameState): GameState =
    gameState.bullets.values
      .filter(_.teamId != teamId)
      .foldLeft(gameState)({
        case (gs, bullet) =>
          val newPos = bullet.currentPosition(time - bullet.time)
          gs.withBullet(
            bullet.id,
            time,
            new Bullet(
              bullet.id,
              time,
              bullet.ownerId,
              bullet.teamId,
              newPos.re,
              newPos.im,
              bullet.radius,
              bullet.direction,
              bullet.speed / 2,
              bullet.currentTravelledDistance(time)
            )
          )
      })

  def end(gameState: GameState): GameState = gameState

}
