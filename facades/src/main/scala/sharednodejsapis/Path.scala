package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
@js.native
@JSImport("path", JSImport.Namespace)
object Path extends js.Object {

  def join(strings: String*): String = js.native

  def resolve(strings: String*): String = js.native

  def dirname(path: String): String = js.native

}
