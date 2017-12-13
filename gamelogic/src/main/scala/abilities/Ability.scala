package abilities

import gamestate.{GameAction, GameState}

/**
 * An Ability is a power that some entities can have (humans or AI) that have an effect on the game.
 *
 * An Ability creates one or several [[gamestate.GameAction]](s).
 */
trait Ability {

  // id created for each ability that is used
  val useId: Long

  // unique identifier of the Ability
  // the id is given manually in the code, and has to be unique.
  val id: Int

  // the cooldown is the minimum amount of time between two ability usages.
  val cooldown: Long

  // the castingTime is the time necessary to use the ability. Defaults to 0, which mean instant ability.
  val castingTime: Long = 0

  val casterId: Long

  // the time at which the Ability was cast.
  val time: Long

  // when an ability is used, it creates a List of GameAction that will apply to the game state
  def createActions: List[GameAction]

  def copyWithUseId(newUseId: Long, newTime: Long): Ability

  /**
   * Returns whether the action is legal with that gameState.
   * This should not check if the ability is still on cooldown.
   * We should check, however, if the player is still alive. Maybe in the Future we will allow actions for dead players.
   */
  def isLegal(gameState: GameState): Boolean

}


object Ability {

  private var lastId: Long = 0: Long
  def newId(): Long = {
    lastId += 1
    lastId
  }

  val activateShieldId: Int = 1
  val bigBulletId: Int = 2
  val tripleBulletId: Int = 3
  val teleportationId: Int = 4
  val createHealingZoneId: Int = 5
  val createBulletAmplifierId: Int = 6
  val launchSmashBulletId: Int = 7
  val craftGunTurretId: Int = 8
  val createBarrierId: Int = 9
  val putBulletGlue: Int = 10

  val abilityNames: Map[Int, String] = Map(
    activateShieldId -> "Shield", bigBulletId -> "Big Bullet",
    tripleBulletId -> "Penta Shot", teleportationId -> "Teleportation",
    createHealingZoneId -> "Healing Zone", createBulletAmplifierId -> "Bullet Amplifier",
    launchSmashBulletId -> "Smash Bullet", craftGunTurretId -> "Gun Turret",
    createBarrierId -> "Barrier", putBulletGlue -> "Bullet Glue"
  )

  val playerChoices: List[Int] = List(
    bigBulletId, tripleBulletId, teleportationId, createHealingZoneId, createBulletAmplifierId,
    launchSmashBulletId, craftGunTurretId, createBarrierId, putBulletGlue
  )

}