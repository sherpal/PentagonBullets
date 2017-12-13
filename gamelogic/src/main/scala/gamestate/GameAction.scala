package gamestate


/**
 * A GameAction allows to go from one GameState to an other.
 * Any action that can be taken during the game has to have its GameAction.
 */
trait GameAction extends Ordered[GameAction] {

  /** Unique identifier of the action, given by the server. */
  val actionId: Long

  def setId(newId: Long): GameAction

  /**
   * The apply method transforms the GameState by first transforming this action under all ActionChanger that the
   * gameState currently possesses, then it construct the new GameState by applying all the default behaviour of the
   * newly created actions.
   */
  def apply(gameState: GameState): GameState =
    if (canHappen(gameState)) transformedGameActions(gameState: GameState).foldLeft(gameState)(
      (gs: GameState, action: GameAction) => action.applyDefault(gs)
    )
    else gameState

  def applyDefault(gameState: GameState): GameState

  def canHappen(gameState: GameState): Boolean = true

  def transformedGameActions(gameState: GameState): List[GameAction] =
    gameState.applyActionChangers(this)

  /**
   * The time at which the action took place. Most actions are commutative, but not all of them, so we need to be
   * careful
   */
  val time: Long

  val actionSource: ActionSource

  override def compare(that: GameAction): Int = this.time.compare(that.time)

  def changeTime(newTime: Long): GameAction


}

object GameAction {

  private var lastId: Long = 0

  def newId(): Long = {
    lastId += 1
    lastId
  }

}
