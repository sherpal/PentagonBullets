package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|


@js.native
@JSImport("fs", JSImport.Namespace)
object FileSystem extends js.Object {

  def readFile(path: String, callback: js.Function2[js.Error, js.UndefOr[Buffer], Any]): Unit = js.native

  def writeFile(path: String, data: String | Buffer, callback: js.Function1[js.Error, Any]): Unit = js.native

  def open(path: String, flags: String | Int, callback: js.Function2[js.Error, js.UndefOr[Int], Any]): Unit = js.native

  def close(fd: Int, callback: js.Function1[js.Error, Any]): Unit = js.native

  def mkdir(path: String, callback: js.Function1[js.Error, Any]): Unit = js.native

  def unlink(path: String, callback: js.Function1[js.Error, Any]): Unit = js.native

}
