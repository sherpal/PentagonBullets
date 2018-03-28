package sharednodejsapis.process

import sharednodejsapis.EventEmitter

import scala.scalajs.js

@js.native
trait Stdin extends EventEmitter {

  def resume(): Unit = js.native

}
