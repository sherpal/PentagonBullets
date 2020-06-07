package entities

import abilities.Ability

/**
  * An WithAbilities Entity has a list of castable ability.
  */
trait WithAbilities extends Entity {

  val allowedAbilities: List[Int]

  def hasAbility(id: Int): Boolean = allowedAbilities.contains(id)

  // the relevant last abilities that where used, essentially to check if some ability is still on cooldown.
  val relevantUsedAbilities: Map[Long, Ability]

  def mayCast(abilityId: Int, currentTime: Long): Boolean =
    hasAbility(abilityId) &&
      relevantUsedAbilities.values.filter(_.id == abilityId).forall(_.isUp(this, currentTime))

}
