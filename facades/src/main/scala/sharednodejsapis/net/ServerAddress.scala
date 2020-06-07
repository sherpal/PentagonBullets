package sharednodejsapis.net

import scala.scalajs.js

trait ServerAddress extends js.Object {

  val port: Int

  val family: String

  val address: String

}
