package entitiescollections

import entities.Player
import gamestate.GameState

/**
 * A PlayerTeam is a group of [[Player]]s that are together.
 * Bad actions from a player does not affect other players in the team.
 *   Example: no collision between a bullet from player A with player B if they belong to the same team.
 * Good actions from a player does affect other players in the team.
 *
 * A Player can NOT belong to more than one team.
 */
class PlayerTeam(val teamNbr: Int, val playerIds: Seq[Long]) {

  val nbrOfPlayers: Int = playerIds.length

  def leader: Long = playerIds.head

  def contains(player: Player): Boolean = contains(player.id)

  def contains(playerId: Long): Boolean = playerIds.contains(playerId)

  def alive(gameState: GameState): Boolean = playerIds.exists(gameState.players.isDefinedAt)

}

