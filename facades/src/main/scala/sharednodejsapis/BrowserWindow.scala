package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


/**
 * https://github.com/electron/electron/blob/master/docs/api/browser-window.md
 */
@js.native
@JSImport("electron", "remote.BrowserWindow")
class BrowserWindow(options: BrowserWindowOptions) extends EventEmitter {


  val webContents: WebContents = js.native

  val id: Int = js.native

  def close(): Unit = js.native

  def loadURL(url: String): Unit = js.native

  def show(): Unit = js.native

  def hide(): Unit = js.native

  def focus(): Unit = js.native

  def maximize(): Unit = js.native

  def setMenu(menu: js.Object): Unit = js.native //TODO: change this when need of a menu

  def getParentWindow(): BrowserWindow = js.native

  def removeAllListeners(eventType: String): Unit = js.native

  def setPosition(x: Int, y: Int): Unit = js.native

  def setSize(width: Int, height: Int): Unit = js.native

  def flashFrame(flag: Boolean): Unit = js.native

  def setIgnoreMouseEvents(flag: Boolean): Unit = js.native

  def isVisible(): Boolean = js.native

}


@js.native
@JSImport("electron", "remote.BrowserWindow")
object BrowserWindow extends EventEmitter {
  /** Returns Array of [[BrowserWindow]] - An array of all opened browser windows. */
  def getAllWindows(): js.Array[BrowserWindow] = js.native

  /** Returns [[BrowserWindow]] - The window that is focused in this application, otherwise returns null. */
  def getFocusedWindow(): BrowserWindow = js.native

  /** Returns [[BrowserWindow]] - The window that owns the given webContents. */
  def fromWebContents(webContents: WebContents): BrowserWindow = js.native

  /** Returns [[BrowserWindow]] - The window with the given id. */
  def fromId(id: Int): BrowserWindow = js.native

  def addDevToolsExtension(path: String): Unit = js.native

  def removeDevToolsExtension(name: String): Unit = js.native

}

trait BrowserWindowOptions extends js.Object {
  /** Width. Default value: 800. */
  val width: js.UndefOr[Int] = js.undefined
  /** Height. Default value: 600. */
  val height: js.UndefOr[Int] = js.undefined

  /**
   * Boolean (optional) - The width and height would be used as web page's size, which means the actual window's size
   * will include window frame's size and be slightly larger. Default is false.
   */
  val useContentSize: js.UndefOr[Boolean] = js.undefined

  /** Integer (optional) - Window's minimum width. Default is 0. */
  val minWidth: js.UndefOr[Int] = js.undefined

  /** Integer (optional) - Window's minimum height. Default is 0. */
  val minHeight: js.UndefOr[Int] = js.undefined

  /**  (NativeImage | String) (optional) - The window icon. On Windows it is recommended to use ICO icons to get best
   * visual effects, you can also leave it undefined so the executable's icon will be used.
   */
  val icon: js.UndefOr[String] = js.undefined

  /** Whether BrowserWindow will be visible. Default value: true. */
  val show: js.UndefOr[Boolean] = js.undefined

  /** Whether BrowserWindow will have encircling Frame. Default value: true. */
  val frame: js.UndefOr[Boolean] = js.undefined

  /** Parent of the BrowserWindow */
  val parent: js.UndefOr[BrowserWindow] = js.undefined

  /** Whether BrowserWindow is modal. Default value: false. */
  val modal: js.UndefOr[Boolean] = js.undefined

  /** Whether BrowserWindow is resizable. Default value: true. */
  val resizable: js.UndefOr[Boolean] = js.undefined

  /** Object (optional) - Settings of web page's features. */
  val webPreferences: js.UndefOr[WebPreferences] = js.undefined

  /**
   * Window's background color as Hexadecimal value, like #66CD00 or #FFF or #80FFFFFF (alpha is supported).
   * Default is "#FFF" (white).
   */
  val backgroundColor: js.UndefOr[String] = js.undefined

  val alwaysOnTop: js.UndefOr[Boolean] = js.undefined

  /**
   * Whether the window can be focused. Default is true.
   */
  val focusable: js.UndefOr[Boolean] = js.undefined
}




trait WebPreferences extends js.Object {

  /** Number (optional) - The default zoom factor of the page, 3.0 represents 300%. Default is 1.0. */
  val zoomFactor: js.UndefOr[Double] = js.undefined

}
