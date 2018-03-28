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


object GunTurretHealthBar {

  private var gameHandler: GameHandler = _

  def setGameHandler(gH: GameHandler): Unit =
    gameHandler = gH

  private val turretHealthBars: mutable.Set[GunTurretHealthBar] = mutable.Set()

  private val watchingFrame: Frame = new Frame()
  watchingFrame.registerEvent(GameEvents.OnNewGunTurret)((action: NewGunTurret, _) => {
    val bar = new GunTurretHealthBar(action.turretId, gameHandler)
    turretHealthBars += bar
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
