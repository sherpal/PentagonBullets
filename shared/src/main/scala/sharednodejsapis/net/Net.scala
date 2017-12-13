package sharednodejsapis.net

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("net", JSImport.Namespace)
object Net extends js.Object {

  /**
   * The specified callback will be directly bound to the listener of "connection".
   */
  def createServer(callback: js.Function1[TCPSocket, Unit] = js.native): TCPServer = js.native

}
