package abilities
import entities.WithAbilities


/**
 * A MultiStepAbility is a specification that can have [[Ability]]'s specifying that the ability occurs in multiple
 * times.
 *
 * A MultiStepAbility has inner cooldown between each step.
 *
 * Example: The laser. First time the ability is used, is pops the laser source on the ground. Then, when the ability
 * is used, the laser is activated.
 */
trait MultiStepAbility extends Ability {

  val stepNumber: Int

  /** Cooldown between the steps. innerCooldown(n) is the cooldown from step n to step n + 1 (mod number of steps) */
  val innerCooldown: Vector[Long]

  override def isUp(caster: WithAbilities, now: Long, allowedError: Long = 0): Boolean =
    now - time + allowedError >= innerCooldown(stepNumber) / caster.allowedAbilities.count(_ == id)

}
