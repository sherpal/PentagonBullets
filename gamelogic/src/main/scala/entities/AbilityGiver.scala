package entities

import physics.Circle

/**
  * When a [[Player]] catches an AbilityGiver, it learns the ability whose ID is abilityId.
  * If it already has the ability, the cooldown will be divided by 2.
  */
class AbilityGiver(val id: Long, val time: Long, val xPos: Double, val yPos: Double, val abilityId: Int) extends Body {

  val rotation: Double = 0.0

  val shape: Circle = new Circle(AbilityGiver.radius)

}

object AbilityGiver {

  val radius: Int = 20

  def playerTakeAbilityGiver(player: Player, time: Long, abilityId: Int): Player = new Player(
    player.id,
    player.team,
    time,
    player.name,
    player.xPos,
    player.yPos,
    player.direction,
    player.speed,
    player.moving,
    player.rotation,
    player.shape,
    player.lifeTotal,
    player.allowedAbilities :+ abilityId,
    player.relevantUsedAbilities
  )

}
