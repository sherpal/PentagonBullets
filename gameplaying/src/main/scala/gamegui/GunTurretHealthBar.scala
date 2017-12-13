package gamegui

import entities.GunTurret
import gameinfo.{GameEvents, GameHandler}
import gamestate.actions.{DestroyGunTurret, NewGunTurret}
import gui._

import scala.collection.mutable


class GunTurretHealthBar private (val turretId: Long, gameHandler: GameHandler) extends HealthBar {

  def unit: Option[GunTurret] = gameHandler.currentGameState.gunTurrets.get(turretId)

  HealthBar.addBar(this)

}

//class GunTurretHealthBar private (turretId: Long, gameHandler: GameHandler) extends StatusBar {
//  setSize(GunTurret.defaultRadius * 2 - 5, 5)
//  setPoint(Center)
//
//  def turret: GunTurret = gameHandler.currentGameState.gunTurrets(turretId)
//
//  registerEvent(GameEvents.OnGunTurretTakesDamage)((action: GunTurretTakesDamage, state: GameState) => {
//    if (action.turretId == turretId && state.isLivingUnitAlive(action.turretId)) {
//      setValue(turret.lifeTotal)
//    }
//  })
//
//  registerEvent(GameEvents.OnDestroyGunTurret)((action: DestroyGunTurret, _) => {
//    if (action.turretId == turretId) {
//      hide()
//      GunTurretHealthBar.turretHealthBars -= this
//    }
//  })
//
//  setScript(ScriptKind.OnValueChanged)((newValue: Double, prevValue: Double) => {
//    if (newValue != prevValue) {
//      if (newValue <= GunTurret.maxLifeTotal / 5) {
//        setStatusBarColor(1,0,0)
//      } else if (newValue <= GunTurret.maxLifeTotal / 2) {
//        setStatusBarColor(1, 163 / 255.0, 0)
//      } else {
//        setStatusBarColor(0,1,0)
//      }
//    }
//  })
//
//  setScript(ScriptKind.OnUpdate)((_: Double) => {
//    clearAllPoints()
//    val gameState = gameHandler.currentGameState
//    if (gameState.isLivingUnitAlive(turretId)) {
//      val p = gameState.gunTurrets(turretId)
//      val worldPos = p.pos
//      val pos = worldPos - EntityDrawer.camera.worldCenter
//      setPoint(
//        Top, UIParent, Center,
//        pos.re * EntityDrawer.camera.scaleX,
//        (pos.im - GunTurret.defaultRadius - 5) * EntityDrawer.camera.scaleY
//      )
//    } else {
//      hide()
//    }
//  })
//
//
//  private val bg: GUISprite = createSprite(layer = BackgroundLayer)
//  bg.setVertexColor(1,1,1)
//  bg.setAllPoints()
//
//  setStatusBarTexture()
//  setStatusBarColor(0,1,0)
//  setMinMaxValues(0, GunTurret.maxLifeTotal)
//  setValue(turret.lifeTotal)
//
//
//}


object GunTurretHealthBar {

  private var gameHandler: GameHandler = _

  def setGameHandler(gH: GameHandler): Unit =
    gameHandler = gH

  private val turretHealthBars: mutable.Set[GunTurretHealthBar] = mutable.Set()

  private val watchingFrame: Frame = new Frame()
  watchingFrame.registerEvent(GameEvents.OnNewGunTurret)((action: NewGunTurret, _) => {
    val bar = new GunTurretHealthBar(action.turretId, gameHandler)
    turretHealthBars += bar
//    bar.clearAllPoints()
//    val pos = action.pos - EntityDrawer.camera.worldCenter
//    bar.setPoint(
//      Top, UIParent, Center,
//      pos.re * EntityDrawer.camera.scaleX,
//      (action.pos.im - GunTurret.defaultRadius - 3) * EntityDrawer.camera.scaleY
//    )
  })

  watchingFrame.registerEvent(GameEvents.OnDestroyGunTurret)((action: DestroyGunTurret, _) => {
    turretHealthBars.find(_.turretId == action.turretId) match {
      case Some(bar) =>
        HealthBar.removeBar(bar)
        turretHealthBars -= bar
      case _ =>
    }
  })

}
