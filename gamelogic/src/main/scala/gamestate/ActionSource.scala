package gamestate

import scala.language.implicitConversions

/**
  * An ActionSource determines what created a GameAction in the first place.
  *
  * It can be
  * - the player, for example UpdatePlayerPos, NewBullet or UseAbilityAction
  * - the server, for collisions, UpdatePlayerPos when player needs to be replaced...
  * - an Ability, for example the NewBullets of a MultiShot, the UpdatePlayerPos of a Teleportation
  * - a GunTurret, for NewBullets
  */
trait ActionSource

object ActionSource {

  case object PlayerSource extends ActionSource
  case object ServerSource extends ActionSource
  case object AbilitySource extends ActionSource
  case object GunTurretSource extends ActionSource

  val stringToActionSource: Map[String, ActionSource] = List(
    PlayerSource,
    ServerSource,
    AbilitySource,
    GunTurretSource
  ).map(source => source.toString -> source).toMap

  implicit def fromString(str: String): ActionSource = stringToActionSource(str)

}
