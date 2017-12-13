package graphics

import time.Time

import scala.collection.mutable


trait Animation {

  private var _startTime: Long = 0

  def startTime: Long = _startTime

  /**
   * If set to null, it is a never ending animation.
   */
  val duration: Option[Long]

  protected def animate(currentTime: Long, camera: Camera): Unit

  def stopRunning(): Unit = Animation.runningAnimations -= this

  def run(): Unit = {
    _startTime = Time.getTime
    Animation.runningAnimations += this
  }

}

object Animation {

  private val runningAnimations: mutable.Set[Animation] = mutable.Set()

  def animate(currentTime: Long, camera: Camera): Unit = {
    runningAnimations.filter(animation =>
      animation.duration.isDefined && animation.duration.get < currentTime - animation.startTime
    ).toSeq.foreach(_.stopRunning())

    runningAnimations.foreach(_.animate(currentTime, camera))
  }

}
