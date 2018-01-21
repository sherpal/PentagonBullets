package sharednodejsapis.tls

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("tls", JSImport.Namespace)
object TLS extends js.Object {

  def createServer(options: TLSServerOptions): TLSServer = js.native

  def connect(options: TLSConnectOptions): TLSSocket = js.native

}
