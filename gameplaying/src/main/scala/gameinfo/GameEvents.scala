package gameinfo

import gamestate.actions._
import gamestate.{GameAction, GameState}
import gui.{Frame, ScriptKind, ScriptObject}

object GameEvents {

  val OnActionTaken: ScriptKind { type Handler = (GameAction, GameState) => Unit } =
    ScriptKind.makeEvent[(GameAction, GameState) => Unit]

  val OnChangeBulletRadius: ScriptKind { type Handler = (ChangeBulletRadius, GameState) => Unit } =
    ScriptKind.makeEvent[(ChangeBulletRadius, GameState) => Unit]

  val OnDestroyGunTurret: ScriptKind { type Handler = (DestroyGunTurret, GameState) => Unit } =
    ScriptKind.makeEvent[(DestroyGunTurret, GameState) => Unit]

  val OnFireLaser: ScriptKind { type Handler = (FireLaser, GameState) => Unit } =
    ScriptKind.makeEvent[(FireLaser, GameState) => Unit]

  val OnGameBegins: ScriptKind { type Handler = (GameBegins, GameState) => Unit } =
    ScriptKind.makeEvent[(GameBegins, GameState) => Unit]

  val OnGunTurretTakesDamage: ScriptKind { type Handler = (GunTurretTakesDamage, GameState) => Unit } =
    ScriptKind.makeEvent[(GunTurretTakesDamage, GameState) => Unit]

  val OnHealingZoneHeals: ScriptKind { type Handler = (HealingZoneHeals, GameState) => Unit } =
    ScriptKind.makeEvent[(HealingZoneHeals, GameState) => Unit]

  val OnNewBullet: ScriptKind { type Handler = (NewBullet, GameState) => Unit } =
    ScriptKind.makeEvent[(NewBullet, GameState) => Unit]

  val OnNewGunTurret: ScriptKind { type Handler = (NewGunTurret, GameState) => Unit } =
    ScriptKind.makeEvent[(NewGunTurret, GameState) => Unit]

  val OnNewLaserLauncher: ScriptKind { type Handler = (NewLaserLauncher, GameState) => Unit } =
    ScriptKind.makeEvent[(NewLaserLauncher, GameState) => Unit]

  val OnNewPlayer: ScriptKind { type Handler = (NewPlayer, GameState) => Unit } =
    ScriptKind.makeEvent[(NewPlayer, GameState) => Unit]

  val OnPlayerBringsBackFlag: ScriptKind { type Handler = (PlayerBringsFlagBack, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerBringsFlagBack, GameState) => Unit]

  val OnPlayerDead: ScriptKind { type Handler = (PlayerDead, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerDead, GameState) => Unit]

  val OnPlayerHitByBullet: ScriptKind { type Handler = (PlayerHitByBullet, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerHitByBullet, GameState) => Unit]

  val OnPlayerHitByMultipleBullets: ScriptKind { type Handler = (PlayerHitByMultipleBullets, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerHitByMultipleBullets, GameState) => Unit]

  val OnPlayerHitBySmashBullet: ScriptKind { type Handler = (PlayerHitBySmashBullet, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerHitBySmashBullet, GameState) => Unit]

  val OnPlayerTakeAbilityGiver: ScriptKind { type Handler = (PlayerTakeAbilityGiver, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerTakeAbilityGiver, GameState) => Unit]

  val OnPlayerTakeDamage: ScriptKind { type Handler = (PlayerTakeDamage, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerTakeDamage, GameState) => Unit]

  val OnPlayerTakeHealUnit: ScriptKind { type Handler = (PlayerTakeHealUnit, GameState) => Unit } =
    ScriptKind.makeEvent[(PlayerTakeHealUnit, GameState) => Unit]

  val OnSmashBulletGrows: ScriptKind { type Handler = (SmashBulletGrows, GameState) => Unit } =
    ScriptKind.makeEvent[(SmashBulletGrows, GameState) => Unit]

  val OnUpdatePlayerPos: ScriptKind { type Handler = (UpdatePlayerPos, GameState) => Unit } =
    ScriptKind.makeEvent[(UpdatePlayerPos, GameState) => Unit]

  val OnUseAbilityAction: ScriptKind { type Handler = (UseAbilityAction, GameState) => Unit } =
    ScriptKind.makeEvent[(UseAbilityAction, GameState) => Unit]

  def registerAllEvents(frame: Frame, handler: (GameAction, GameState) => Unit): Unit = {
    frame.registerEvent(OnChangeBulletRadius)(handler)
    frame.registerEvent(OnDestroyGunTurret)(handler)
    frame.registerEvent(OnFireLaser)(handler)
    frame.registerEvent(OnGameBegins)(handler)
    frame.registerEvent(OnGunTurretTakesDamage)(handler)
    frame.registerEvent(OnHealingZoneHeals)(handler)
    frame.registerEvent(OnNewGunTurret)(handler)
    frame.registerEvent(OnNewLaserLauncher)(handler)
    frame.registerEvent(OnPlayerBringsBackFlag)(handler)
    frame.registerEvent(OnPlayerDead)(handler)
    frame.registerEvent(OnPlayerHitByBullet)(handler)
    frame.registerEvent(OnPlayerHitByMultipleBullets)(handler)
    frame.registerEvent(OnPlayerHitBySmashBullet)(handler)
    frame.registerEvent(OnPlayerTakeAbilityGiver)(handler)
    frame.registerEvent(OnPlayerTakeDamage)(handler)
    frame.registerEvent(OnPlayerTakeHealUnit)(handler)
    frame.registerEvent(OnSmashBulletGrows)(handler)
    frame.registerEvent(OnUseAbilityAction)(handler)
    frame.registerEvent(OnUpdatePlayerPos)(handler)
    frame.registerEvent(OnNewBullet)(handler)
    frame.registerEvent(OnNewPlayer)(handler)
  }

  def fireGameEvent(action: GameAction, gameState: GameState): Unit = action match {
    case action: UpdatePlayerPos            => ScriptObject.firesEvent(OnUpdatePlayerPos)(action, gameState)
    case action: NewBullet                  => ScriptObject.firesEvent(OnNewBullet)(action, gameState)
    case action: PlayerHitByBullet          => ScriptObject.firesEvent(OnPlayerHitByBullet)(action, gameState)
    case action: PlayerHitByMultipleBullets => ScriptObject.firesEvent(OnPlayerHitByMultipleBullets)(action, gameState)
    case action: PlayerTakeAbilityGiver     => ScriptObject.firesEvent(OnPlayerTakeAbilityGiver)(action, gameState)
    case action: PlayerTakeDamage           => ScriptObject.firesEvent(OnPlayerTakeDamage)(action, gameState)
    case action: PlayerTakeHealUnit         => ScriptObject.firesEvent(OnPlayerTakeHealUnit)(action, gameState)
    case action: UseAbilityAction           => ScriptObject.firesEvent(OnUseAbilityAction)(action, gameState)
    case action: HealingZoneHeals           => ScriptObject.firesEvent(OnHealingZoneHeals)(action, gameState)
    case action: ChangeBulletRadius         => ScriptObject.firesEvent(OnChangeBulletRadius)(action, gameState)
    case action: PlayerHitBySmashBullet     => ScriptObject.firesEvent(OnPlayerHitBySmashBullet)(action, gameState)
    case action: GunTurretTakesDamage       => ScriptObject.firesEvent(OnGunTurretTakesDamage)(action, gameState)
    case action: NewGunTurret               => ScriptObject.firesEvent(OnNewGunTurret)(action, gameState)
    case action: DestroyGunTurret           => ScriptObject.firesEvent(OnDestroyGunTurret)(action, gameState)
    case action: SmashBulletGrows           => ScriptObject.firesEvent(OnSmashBulletGrows)(action, gameState)
    case action: NewLaserLauncher           => ScriptObject.firesEvent(OnNewLaserLauncher)(action, gameState)
    case action: FireLaser                  => ScriptObject.firesEvent(OnFireLaser)(action, gameState)
    case action: PlayerBringsFlagBack       => ScriptObject.firesEvent(OnPlayerBringsBackFlag)(action, gameState)
    case action: GameBegins                 => ScriptObject.firesEvent(OnGameBegins)(action, gameState)
    case action: PlayerDead                 => ScriptObject.firesEvent(OnPlayerDead)(action, gameState)
    case action: NewPlayer                  => ScriptObject.firesEvent(OnNewPlayer)(action, gameState)
    case _                                  => //ScriptObject.firesEvent(OnActionTaken)(action, gameState)
    //println(s"Action ${action.getClass} is not yet implemented :(")
  }

}
