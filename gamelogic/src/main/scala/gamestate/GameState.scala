package gamestate

import abilities.Ability
import custommath.Complex
import entities._
import gamestate.ActionSource.PlayerSource
import gamestate.actions.{NewBullet, UpdatePlayerPos}
import physics.{Polygon, Shape}
import time.Time

/**
 * A GameStates owns all the information needed to continue a Game.
 * Any information must be retrievable through the values of the state.
 *
 * Small optimization for the future:
 * Make GameState a trait, with all the "withStuff" and "removeStuff" left abstract, and implement subclasses depending
 * on what's actually in the game. Then, a "withStuff" can either just throw an exception if adding stuff that is not
 * in this game, or create an instance corresponding to the one with that stuff added. Probably the first option though.
 */
class GameState(val time: Long, val startTime: Option[Long],
                val gameBounds: Polygon, // determines what is in the game.
                val players: Map[Long, Player],
                val deadPlayers: Map[Long, Player],
                val bullets: Map[Long, Bullet],
                val obstacles: Map[Long, Obstacle],
                val healUnits: Map[Long, HealUnit],
                val gunTurrets: Map[Long, GunTurret],
                val damageZones: Map[Long, DamageZone],
                val healingZones: Map[Long, HealingZone],
                val abilityGivers: Map[Long, AbilityGiver],
                val bulletAmplifiers: Map[Long, BulletAmplifier],
                val barriers: Map[Long, Barrier],
                val smashBullets: Map[Long, SmashBullet],
                val mists: Map[Long, Mist],
                val actionChangers: Map[Long, ActionChanger],
                val flags: Map[Int, TeamFlag] // flags are mapped from their team number
               ) {

  def apply(actions: Seq[GameAction]): GameState = actions.foldLeft(this)(
    (state: GameState, action: GameAction) => action(state)
  )

  def started: Boolean = startTime.isDefined

  import gamestate.GameState._

  def state: StateOfGame = startTime match {
    case None =>
      PreBegin
    case Some(0) => // We put the startTime to Some(0) when the game ends, hence this check.
      GameEnded
    case _ =>
      PlayingState
  }

//  def state: StateOfGame = if (!started)
//    PreBegin
//  else if (players.values.map(_.team).toList.distinct.size > 1)
//    PlayingState
//  else
//    GameEnded

  def isLegalAction: (GameAction) => Boolean = {
    case action: UpdatePlayerPos =>
      isPlayerAlive(action.playerId) && {
        val player = players(action.playerId)
        val maxDistance = (action.time - player.time) * (player.speed + 1)
        (Complex(action.x, action.y) - player.pos).modulus2 <= maxDistance * maxDistance
      }
    case action: NewBullet if action.actionSource == PlayerSource =>
      isPlayerAlive(action.playerId) && {
        val player = players(action.playerId)
        val maxDistance = (action.time - player.time) * (player.speed + 1) + Player.radius
        (action.pos - player.pos).modulus2 <= maxDistance * maxDistance
      }
    case _ => true
  }

  def isLegalAbilityUse(ability: Ability): Boolean = {
    withAbilities.get(ability.casterId) match {
      case Some(entity) =>
        entity.mayCast(ability.id, time) && ability.isLegal(this)
      case None =>
        false
    }
  }

  def isPlayerAlive(id: Long): Boolean = players.isDefinedAt(id)

  def isPlayerAlive(name: String): Boolean = players.values.exists(_.name == name)

  /**
   * This will be used in the Future if we want to add more types of Entities that are living, like mobs.
   */
  def isLivingUnitAlive(id: Long): Boolean = players.isDefinedAt(id) || gunTurrets.isDefinedAt(id)

  def entities: Set[Entity] = List(
    players.values.toSet[Entity],
    bullets.values.toSet[Entity]
  ).fold(Set[Entity]())(_ union _)

  def collidingPlayerObstacles(player: Player): Iterable[Body] = collidingPlayerObstacles(player.team)

  def collidingPlayerObstacles(playerTeam: Int): Iterable[Body] =
    barriers.values.filter(_.teamId != playerTeam) ++ obstacles.values

  def withAbilities: Map[Long, WithAbilities] = players

  def withPlayer(id: Long, time: Long, player: Player): GameState = new GameState(
    time, startTime, gameBounds,
    players + (id -> player), deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withDeadPlayer(id: Long, time: Long, player: Player): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers + (id -> player), bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withBullet(id: Long, time: Long, bullet: Bullet): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets + (id -> bullet), obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withObstacle(id: Long, time: Long, obstacle: Obstacle): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles + (id -> obstacle), healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withHealUnit(id: Long, time: Long, healUnit: HealUnit): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits + (id -> healUnit), gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withGunTurret(id: Long, time: Long, gunTurret: GunTurret): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets + (id -> gunTurret), damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withDamageZone(id: Long, time: Long, damageZone: DamageZone): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones + (id -> damageZone), healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withHealingZone(id: Long, time: Long, healingZone: HealingZone): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones + (id -> healingZone),
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withAbilityGiver(id: Long, time: Long, abilityGiver: AbilityGiver): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers + (id -> abilityGiver), bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def withBulletAmplifier(id: Long, time: Long, bulletAmplifier: BulletAmplifier): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers + (id -> bulletAmplifier), barriers, smashBullets, mists, actionChangers, flags
  )

  def withBarrier(id: Long, time: Long, barrier: Barrier): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers + (id -> barrier), smashBullets, mists, actionChangers, flags
  )

  def withSmashBullet(id: Long, time: Long, smashBullet: SmashBullet): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets + (id -> smashBullet), mists, actionChangers, flags
  )

  def withMist(id: Long, time: Long, mist: Mist): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists + (id -> mist), actionChangers, flags
  )

  def withActionChanger(id: Long, time: Long, actionChanger: ActionChanger): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers + (id -> actionChanger), flags
  )

  def withFlag(time: Long, flag: TeamFlag): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags + (flag.teamNbr -> flag)
  )

  def removePlayer(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players - id, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeDeadPlayer(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers - id, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeBullet(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets - id, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeHealUnit(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits - id, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeGunTurret(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets - id, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeDamageZone(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones - id, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeHealingZone(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones - id,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeAbilityGiver(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers - id, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeBulletAmplifier(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers - id, barriers, smashBullets, mists, actionChangers, flags
  )

  def removeBarrier(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers - id, smashBullets, mists, actionChangers, flags
  )

  def removeSmashBullet(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets - id, mists, actionChangers, flags
  )

  def removeActionChanger(id: Long, time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers - id, flags
  )

  def start(time: Long, bounds: Polygon): GameState = new GameState(
    time, Some(time), bounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def ends(time: Long): GameState = new GameState(
    time, Some(0: Long), gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )

  def timeUpdate(time: Long): GameState = new GameState(
    time, startTime, gameBounds,
    players, deadPlayers, bullets, obstacles, healUnits, gunTurrets, damageZones, healingZones,
    abilityGivers, bulletAmplifiers, barriers, smashBullets, mists, actionChangers, flags
  )


  def applyActionChangers(action: GameAction): List[GameAction] = {
    applyActionChangers(List(action))
//    actionChangers.values.foldLeft(List(action))(
//      (as: List[GameAction], changer: ActionChanger) => as.flatMap(changer.changeAction))
  }

  def applyActionChangers(actions: List[GameAction]): List[GameAction] = {
    val changers = actionChangers.values
    actions.flatMap(action => changers.foldLeft(List(action))(
      (as: List[GameAction], changer: ActionChanger) => as.flatMap(changer.changeAction)))
  }





}


object GameState {

  def originalState: GameState = new GameState(
    Time.getTime - 1000, None,
    Shape.regularPolygon(4, 100), // putting a random shape
    Map[Long, Player](),
    Map[Long, Player](),
    Map[Long, Bullet](),
    Map[Long, Obstacle](),
    Map[Long, HealUnit](),
    Map[Long, GunTurret](),
    Map[Long, DamageZone](),
    Map[Long, HealingZone](),
    Map[Long, AbilityGiver](),
    Map[Long, BulletAmplifier](),
    Map[Long, Barrier](),
    Map[Long, SmashBullet](),
    Map[Long, Mist](),
    Map[Long, ActionChanger](),
    Map[Int, TeamFlag]()
  )

  sealed trait StateOfGame
  case object PreBegin extends StateOfGame
  case object PlayingState extends StateOfGame
  case object GameEnded extends StateOfGame
}


