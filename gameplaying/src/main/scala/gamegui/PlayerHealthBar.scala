package gamegui

import entities.Player


class PlayerHealthBar(playerId: Long, getPlayer: () => Option[Player]) extends HealthBar {

  def unit: Option[Player] = getPlayer()

  HealthBar.addBar(this)

}

