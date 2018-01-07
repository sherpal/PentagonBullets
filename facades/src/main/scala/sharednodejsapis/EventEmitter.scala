package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSGlobal}


@js.native
@JSGlobal
class EventEmitter extends js.Object {
  def on(eventName: String, listener: js.Function): Unit = js.native

  def once(eventName: String, listener: js.Function): Unit = js.native

}


@js.native
@JSImport("events", JSImport.Namespace)
object EventModule extends js.Object {
  def createSocket(t: String): Socket = js.native
}
