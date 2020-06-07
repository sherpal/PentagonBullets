package sharednodejsapis.net

import sharednodejsapis.EventEmitter

import scala.scalajs.js

/**
  * https://nodejs.org/dist/latest-v9.x/docs/api/net.html#net_net_createserver_options_connectionlistener
  *
  * Possible events:
  * - close
  * - connection: receives the abstract TCPSocket
  * - error
  * - listening
  */
@js.native
trait TCPServer extends EventEmitter {

  def address(): ServerAddress = js.native

  def close(callback: js.Function0[Unit] = js.native): Unit = js.native

  /**
    * The callback will be directly bound to the listener of "listening"
    */
  def listen(port: Int, address: String, callback: js.Function0[Unit] = js.native): TCPServer = js.native

  def listening: Boolean = js.native

  var maxConnections: Int = js.native

}
