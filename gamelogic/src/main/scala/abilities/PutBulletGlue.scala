package abilities
import entities.{BulletGlue, Entity}
import gamestate.ActionSource.AbilitySource
import gamestate.actions.NewActionChanger
import gamestate.{GameAction, GameState}

final class PutBulletGlue(val time: Long, val useId: Long, val casterId: Long, val teamId: Int) extends Ability {

  val id: Int = Ability.putBulletGlue

  val cooldown: Long = 20000

  def copyWithUseId(newUseId: Long, newTime: Long): Ability = new PutBulletGlue(
    newTime, newUseId, casterId, teamId
  )

  def isLegal(gameState: GameState): Boolean = gameState.isPlayerAlive(casterId)

  def createActions(gameState: GameState): List[GameAction] = List(
    NewActionChanger(GameAction.newId(), time, new BulletGlue(Entity.newId(), time, casterId, teamId), AbilitySource)
  )

}
