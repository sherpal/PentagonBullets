package electron

import sharednodejsapis.{BrowserWindow, BrowserWindowOptions, EventEmitter, WebContents}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
@js.native
@JSImport("electron", "BrowserWindow")
class BrowserWindowMainProcess(options: BrowserWindowOptions) extends BrowserWindow(options)
@js.native
@JSImport("electron", "BrowserWindow")
object BrowserWindowMainProcess extends EventEmitter {

  /** Returns Array of [[BrowserWindow]] - An array of all opened browser windows. */
  def getAllWindows(): js.Array[BrowserWindowMainProcess] = js.native

  /** Returns [[BrowserWindow]] - The window that is focused in this application, otherwise returns null. */
  def getFocusedWindow(): BrowserWindowMainProcess = js.native

  /** Returns [[BrowserWindow]] - The window that owns the given webContents. */
  def fromWebContents(webContents: WebContents): BrowserWindowMainProcess = js.native

  /** Returns [[BrowserWindow]] - The window with the given id. */
  def fromId(id: Int): BrowserWindowMainProcess = js.native

  def addDevToolsExtension(path: String): Unit = js.native

  def removeDevToolsExtension(name: String): Unit = js.native

}
