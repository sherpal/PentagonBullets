package sharednodejsapis.portfinder

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("portfinder", JSImport.Namespace)
object PortFinder extends js.Object {

  def getPort(callback: js.Function2[js.Error, Int, Unit]): Unit = js.native

}
