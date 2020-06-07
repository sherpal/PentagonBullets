package graphics

import gamestate.GameState
import time.Time

import scala.collection.mutable

/**
  * A GameAnimation is like an Animation, but the animate method depends on the GameState.
  */
trait GameAnimation {

  private var running: Boolean = false

  private var _startTime: Long = 0

  def startTime: Long = _startTime

  /**
    * If set to null, it is a never ending animation.
    */
  val duration: Option[Long]

  protected def animate(gameState: GameState, now: Long, camera: Camera): Unit

  def stopRunning(): Unit = {
    stopRunningCallback()
    GameAnimation.runningAnimations -= this
    running = false
  }

  protected def stopRunningCallback(): Unit

  def run(): Unit = {
    _startTime = Time.getTime
    GameAnimation.runningAnimations += this
    running = true
  }

}

object GameAnimation {

  private val runningAnimations: mutable.Set[GameAnimation] = mutable.Set()

  def animate(gameState: GameState, now: Long, camera: Camera): Unit = {
    runningAnimations
      .filter(animation => animation.duration.isDefined && animation.duration.get < now - animation.startTime)
      .toSeq
      .foreach(_.stopRunning())

    runningAnimations.foreach(_.animate(gameState, now, camera))
  }

}
