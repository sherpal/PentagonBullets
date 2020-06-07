package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * OS module of Node.js.
  */
@js.native
@JSImport("os", JSImport.Namespace)
object OSModule extends js.Object {

  def networkInterfaces(): NetworkInterfaces = js.native

}

trait NetworkInterface extends js.Object {
  val address: String
}

trait NetworkInterfaces extends js.Object {
  val lo: js.Array[NetworkInterface]

  val eth0: js.Array[NetworkInterface]
}
