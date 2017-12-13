package entities

import gamestate.{GameAction, GameState}

/**
 * An ActionChanger acts on the set of [[GameAction]]s.
 * An ActionChanger often is the Identity on a large subset of the actions.
 *
 * The contract of the action changing property is that:
 * - if the previous action was legal, then the first of the list must also be
 * - the actions created must all be legal, when applied in the order of the List.
 * - the set of all ActionChangers must be commutative on the set of [[GameAction]]s.
 *
 * It can for example double all damages dealt to a certain Living Entity.
 */
trait ActionChanger extends Entity {

  def changeAction(action: GameAction): List[GameAction]

  // Change all actions that are affected by this ActionChanger
  def changeActions(actions: List[GameAction]): List[GameAction] = {
    actions.flatMap(action => if (action.time < time + duration) changeAction(action) else List(action))
  }

  /**
   * Changes the gameState when this changer appears
   */
  def start(gameState: GameState): GameState

  /**
   * Changes the gameState when this change disappear
   */
  def end(gameState: GameState): GameState



  // time at which the ActionChanger has been created.
  val time: Long

  // the ActionChanger ceases to act after duration ms. If it's everlasting, then duration must be 0.
  val duration: Long

}
