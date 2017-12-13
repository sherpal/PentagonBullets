package sharednodejsapis.net


import sharednodejsapis.EventEmitter

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("net", "Socket")
class TCPSocket() extends EventEmitter {

  def write(data: String, encoding: String = "utf8"): Unit = js.native

  def connect(port: Int, address: String): Unit = js.native

}
