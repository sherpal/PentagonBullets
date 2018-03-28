package networkcom

private[networkcom] object PlatformDependent {

  sealed trait IntervalHandle {
    val handle: Any

    override def equals(that: Any): Boolean = that match {
      case that: IntervalHandle => that.handle == this.handle
      case _ => false
    }

    override def hashCode(): Int = handle.hashCode()
  }

  sealed trait TimeoutHandle{
    val handle: Any

    override def equals(that: Any): Boolean = that match {
      case that: TimeoutHandle => that.handle == this.handle
      case _ => false
    }

    override def hashCode(): Int = handle.hashCode()
  }

  def createSocket(): UDPSocket = ???

  def setTimeout(interval: Long)(body: => Unit): TimeoutHandle = ???

  def setInterval(interval: Long)(body: => Unit): IntervalHandle = ???

  def clearInterval(handle: IntervalHandle): Unit = ???

  def clearTimeout(handle: TimeoutHandle): Unit = ???

}
