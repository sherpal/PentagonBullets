package networkcom

import scala.scalajs.js.JSConverters._
import sharednodejsapis._

import scala.scalajs.js.timers.{SetIntervalHandle, SetTimeoutHandle}
import scala.scalajs.js.typedarray.Uint8Array

private[networkcom] object PlatformDependent {

  class UDPSocketJS(socket: Socket) extends UDPSocket {

    def peer: Peer = {
      val address = socket.address()
      Peer(address.address, address.port)
    }

    def bind(port: Int, address: Option[String] = None): Unit = {
      socket.bind(port, if (address.isDefined) address.get else "")
    }

    def send(message: Array[Byte], port: Int, address: String): Unit = {

      socket.send(
        Buffer.from(message.toJSArray),
        port, address
      )

    }

    def setMessageCallback(callback: (Array[Byte], Peer) => Unit): Unit = {

      socket.on("message", (msg: Buffer, rInfo: RInfo) => {
        val peer = Peer(rInfo.address, rInfo.port)

        val array = new Uint8Array(msg).toArray.map(_.toByte)

        callback(array, peer)
      })

      _onMessage = callback
    }

    def setCloseCallback(callback: () => Unit): Unit = {

      socket.on("close", callback)

      _onClose = callback

    }

    def setListeningCallback(callback: () => Unit): Unit = {

      socket.on("listening", callback)

      _onListening = callback

    }

    socket.on("error", (err: ErrorEvent) => {
      println("server error:")
      println(s"${err.stack}")
      socket.close()
    })


    def close(): Unit =
      socket.close()

  }

  def createSocket(): UDPSocket = new UDPSocketJS(DgramModule.createSocket("udp4"))



  sealed trait TimeoutHandle {
    val handle: Any

    override def equals(that: Any): Boolean = that match {
      case that: TimeoutHandle => that.handle == this.handle
      case _ => false
    }

    override def hashCode(): Int = handle.hashCode()
  }

  private object TimeoutHandle {
    def apply(setTimeoutHandle: SetTimeoutHandle): TimeoutHandle = new TimeoutHandle {
      override val handle: Any = setTimeoutHandle
    }
  }

  sealed trait IntervalHandle {
    val handle: Any

    override def equals(that: Any): Boolean = that match {
      case that: IntervalHandle => that.handle == this.handle
      case _ => false
    }

    override def hashCode(): Int = handle.hashCode()
  }

  private object IntervalHandle {
    def apply(setIntervalHandle: SetIntervalHandle): IntervalHandle = new IntervalHandle {
      override val handle: Any = setIntervalHandle
    }
  }

  def setTimeout(interval: Long)(body: => Unit): TimeoutHandle = TimeoutHandle(
    scala.scalajs.js.timers.setTimeout(interval) {
      body
    }
  )

  def clearTimeout(handle: TimeoutHandle): Unit =
    scala.scalajs.js.timers.clearTimeout(handle.handle.asInstanceOf[SetTimeoutHandle])


  def setInterval(interval: Long)(body: => Unit): IntervalHandle = IntervalHandle(
    scala.scalajs.js.timers.setInterval(interval) {
      body
    }
  )

  def clearInterval(handle: IntervalHandle): Unit =
    scala.scalajs.js.timers.clearInterval(handle.handle.asInstanceOf[SetIntervalHandle])

}
