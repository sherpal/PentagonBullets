package time

/**
 * Used to have some time related facility functions.
 */
object Time {

  def getTime: Long = new java.util.Date().getTime + deltaTime

  private var _deltaTime: Long = 0

  def setDeltaTime(delta: Long): Unit = {
    _deltaTime = delta
    println(s"Delta: $delta")
  }

  def deltaTime: Long = _deltaTime

  def getLocalTime: Long = new java.util.Date().getTime

}
