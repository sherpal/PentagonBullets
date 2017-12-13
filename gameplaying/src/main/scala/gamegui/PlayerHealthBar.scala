package gamegui

import entities.Player
import gameinfo.{GameEvents, GameHandler}
import gamestate.actions._
import gamestate.GameState
import graphics.EntityDrawer
import gui._


class PlayerHealthBar(playerId: Long, gameHandler: GameHandler) extends HealthBar {

  def unit: Option[Player] = gameHandler.currentGameState.players.get(playerId)

  HealthBar.addBar(this)

}

//class PlayerHealthBar(playerId: Long, gameHandler: GameHandler) extends StatusBar {
//  setSize(Player.radius * 2 - 10, 5)
//  setPoint(Center)
//
//  def player: Player = gameHandler.currentGameState.players(playerId)
//
//  registerEvent(GameEvents.OnPlayerHitByBullet)((action: PlayerHitByBullet, state: GameState) => {
//    if (action.playerId == playerId && state.isPlayerAlive(action.playerId)) {
//      setValue(player.lifeTotal)
//    }
//  })
//
//  registerEvent(GameEvents.OnPlayerHitBySmashBullet)((action: PlayerHitBySmashBullet, state: GameState) => {
//    if (action.playerId == playerId && state.isPlayerAlive(action.playerId)) {
//      setValue(player.lifeTotal)
//    }
//  })
//
//  registerEvent(GameEvents.OnPlayerTakeDamage)((action: PlayerTakeDamage, state: GameState) => {
//    if (action.playerId == playerId && state.isPlayerAlive(action.playerId)) {
//      setValue(player.lifeTotal)
//    }
//  })
//
//  registerEvent(GameEvents.OnPlayerTakeHealUnit)((action: PlayerTakeHealUnit, state: GameState) => {
//    if (action.playerId == playerId && state.isPlayerAlive(action.playerId)) {
//      setValue(player.lifeTotal)
//    }
//  })
//
//  registerEvent(GameEvents.OnHealingZoneHeals)((action: HealingZoneHeals, state: GameState) => {
//    if (action.healedUnitId == playerId && state.isPlayerAlive(action.healedUnitId)) {
//      setValue(player.lifeTotal)
//    }
//  })
//
//  setScript(ScriptKind.OnUpdate)((_: Double) => {
//    clearAllPoints()
//    val gameState = gameHandler.currentGameState
//    if (gameState.isPlayerAlive(playerId)) {
//      val p = gameState.players(playerId)
//      val worldPos = p.pos
//      val pos = worldPos - EntityDrawer.camera.worldCenter
//      setPoint(
//        Top, UIParent, Center,
//        pos.re * EntityDrawer.camera.scaleX,
//        (pos.im - Player.radius - 5) * EntityDrawer.camera.scaleY
//      )
//     } else {
//      hide()
//    }
//  })
//
//  setScript(ScriptKind.OnValueChanged)((newValue: Double, prevValue: Double) => {
//    if (newValue != prevValue) {
//      if (newValue <= Player.maxLife / 5) {
//        setStatusBarColor(1,0,0)
//      } else if (newValue <= Player.maxLife / 2) {
//        setStatusBarColor(1, 163 / 255.0, 0)
//      } else {
//        setStatusBarColor(0,1,0)
//      }
//    }
//  })
//
//  private val bg: GUISprite = createSprite(layer = BackgroundLayer)
//  bg.setVertexColor(1,1,1)
//  bg.setAllPoints()
//
//  setStatusBarTexture()
//  setStatusBarColor(0,1,0)
//  setMinMaxValues(0, Player.maxLife)
//  setValue(player.lifeTotal)
//
//
//}
