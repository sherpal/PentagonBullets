package sharednodejsapis.tls

import scala.scalajs.js

class TLSConnectOptions extends CreateSecureContextOptions {

  val host: js.UndefOr[String] = js.undefined

  val port: js.UndefOr[Int] = js.undefined

}
