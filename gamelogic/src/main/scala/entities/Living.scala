package entities

/**
 * A Living Entity has life, can take damage or heal, and can be dead.
 */
trait Living extends Entity {
  val lifeTotal: Double
}
