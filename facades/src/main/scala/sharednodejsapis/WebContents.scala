package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
@js.native
@JSGlobal
abstract class WebContents extends EventEmitter {

  /** Opens the DevTools. */
  def openDevTools(): Unit = js.native

  def send(channel: String, args: Any*): Unit = js.native

  def printToPDF(options: PrintToPDFOptions, callback: js.Function2[js.Error, Buffer, Any]): Unit = js.native

}

trait PrintToPDFOptions extends js.Object {

  /** Specifies the type of margins to use. Uses 0 for default margin, 1 for no margin, and 2 for minimum margin. */
  val marginsType: js.UndefOr[Int] = js.undefined

  /**
    * Specify page size of the generated PDF. Can be A3, A4, A5, Legal, Letter, Tabloid or an Object
    * containing height and width in microns.
    */
  val pageSize: js.UndefOr[String] = js.undefined

  /** Whether to print CSS backgrounds. */
  val printBackground: js.UndefOr[Boolean] = js.undefined

  /** Whether to print selection only. */
  val printSelectionOnly: js.UndefOr[Boolean] = js.undefined

  /** true for landscape, false for portrait. */
  val landscape: js.UndefOr[Boolean] = js.undefined
}
