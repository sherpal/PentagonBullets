package networkcom

import scala.scalajs.js.JSConverters._
import sharednodejsapis._

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


}
