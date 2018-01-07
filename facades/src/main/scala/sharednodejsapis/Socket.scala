package sharednodejsapis

import org.scalajs.dom.Event

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSImport}
import scala.scalajs.js.|


trait ErrorEvent extends Event {
  val stack: String
}


trait RInfo extends js.Object {
  val address: String
  val port: Int
  val family: String
}


trait Address extends js.Object {
  val address: String
  val port: Int
}

@js.native
@JSGlobal("dgram.Socket")
abstract class Socket extends EventEmitter {
  def address(): Address = js.native

  def bind(port: Int, address: String = js.native): Unit = js.native

  def close(): Unit = js.native

  def send(msg: String | Buffer | js.Array[Buffer], port: Int, address: String): Unit = js.native
  def send(msg: String | Buffer | js.Array[Buffer], port: Int, address: String, callback: js.Function): Unit = js.native
  def send(msg: String | Buffer | js.Array[Buffer], offset: Int, length: Int, port: Int, address: String): Unit = js.native
  def send(msg: String | Buffer | js.Array[Buffer], offset: Int, length: Int,
           port: Int, address: String, callback: js.Function): Unit = js.native
}

@js.native
@JSImport("dgram", JSImport.Namespace)
object DgramModule extends js.Object {
  def createSocket(t: String): Socket = js.native
}
