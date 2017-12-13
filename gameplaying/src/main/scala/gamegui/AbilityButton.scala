package gamegui

import abilities.Ability
import gameinfo.GameEvents
import gamestate.actions.UseAbilityAction
import gamestate.GameState
import gui._
import pixigraphics.PIXITexture

/**
 * Small square at the bottom of the screen that allows to track cooldown of player abilities.
 */
class AbilityButton(val abilityId: Int, val playerId: Long, nextTo: Option[Frame] = None) extends Frame() {

  setSize(40,40)

  setScript(ScriptKind.OnUIParentResize)(() => {
    clearAllPoints()
    nextTo match {
      case Some(frame) =>
        setPoint(Left, frame, Right)
      case None =>
        setPoint(Bottom)
    }
  })
  nextTo match {
    case Some(frame) =>
      setPoint(Left, frame, Right)
    case None =>
      setPoint(Bottom)
  }


  private val bg: GUISprite = createSprite()
  bg.setAllPoints()
  bg.setTexture(PIXITexture.fromImage(AbilityButton.images(abilityId)))
  bg.setVertexColor(0, 1, 0)

  private val focusSprite: GUISprite = createSprite(layer = Overlay)
  focusSprite.setAllPoints()
  focusSprite.setTexture(PIXITexture.fromImage("../../assets/ui/ability_focus.png"))
  focusSprite.hide()

  def focus(): Unit =
    focusSprite.show()

  def blur(): Unit =
    focusSprite.hide()

  val statusBar: StatusBar = new StatusBar(this)
  statusBar.setAllPoints()
  statusBar.setStatusBarTexture()
  statusBar.setOrientation(VerticalBar)
  statusBar.setStatusBarColor(0,0,0,0.5)
  statusBar.setValue(0)



  statusBar.setScript(ScriptKind.OnUpdate)((dt: Double) => {
    if (statusBar.value > 0)
      statusBar.setValue(statusBar.value - dt)
  })

  statusBar.setScript(ScriptKind.OnValueChanged)((value: Double, _: Double) => {
    if (value <= 0) {
      bg.setVertexColor(0.3, 1, 0.3)
    }
  })

  registerEvent(GameEvents.OnUseAbilityAction)((action: UseAbilityAction, state: GameState) => {
    if (action.ability.id == abilityId && action.ability.casterId == playerId) {
      bg.setVertexColor(1, 1, 1)
      val effectiveCooldown = action.ability.cooldown / state.players(playerId).allowedAbilities.count(_ == abilityId)
      statusBar.setMinMaxValues(0, effectiveCooldown)
      statusBar.setValue(effectiveCooldown)
    }
  })


}


object AbilityButton {

  val images: Map[Int, String] = Map(
    Ability.activateShieldId -> "../../assets/abilities/shield.png",
    Ability.bigBulletId -> "../../assets/abilities/big_bullet.png",
    Ability.tripleBulletId -> "../../assets/abilities/quad_bullets.png",
    Ability.teleportationId -> "../../assets/abilities/teleportation.png",
    Ability.createHealingZoneId -> "../../assets/abilities/healing_zone.png",
    Ability.createBulletAmplifierId -> "../../assets/abilities/bullet_amplifier.png",
    Ability.launchSmashBulletId -> "../../assets/abilities/smash_bullet.png",
    Ability.craftGunTurretId -> "../../assets/abilities/gun_turret.png",
    Ability.createBarrierId -> "../../assets/abilities/barrier.png",
    Ability.putBulletGlue -> "../../assets/abilities/bullet_glue.png"
  )

}
