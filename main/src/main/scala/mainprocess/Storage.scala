package mainprocess

import electron.MainProcessGlobals
import sharednodejsapis.{IPCMain, IPCMainEvent, Path, WebContents}
import globalvariables.{BaseDirectory, Data, DataStorage}

import scala.collection.mutable
import scala.scalajs.js

/**
  * The Storage object will store all the values that the Renderer processes ask to keep.
  *
  * To use the storage, see VariableStorage object.
  * If you want to need the Storage object, you need to explicitly calling it somewhere, e.g. at the beginning of your
  * main process file.
  */
object Storage {

  private val variables: mutable.Map[WebContents, mutable.Map[String, Any]] = mutable.Map()

  /**
    * This function is not private. It can be used by the main process, e.g., in a "did-finish-load" event to set things
    * that the window will be able to access later.
    */
  def storeVariable(webContents: WebContents, key: String, value: Data): Unit =
    storeRawVariable(webContents, key, DataStorage.encode(value))

  private def storeRawVariable(webContents: WebContents, key: String, value: Any): Unit =
    variables.get(webContents) match {
      case Some(map) => map += (key -> value)
      case None =>
        variables += (webContents -> mutable.Map(key -> value))
    }

  private def retrieveVariable(webContents: WebContents, key: String): Any =
    variables.get(webContents) match {
      case Some(map) => map.getOrElse(key, null)
      case None      => null
    }

  private def unStoreVariable(webContents: WebContents, key: String): Unit =
    variables.get(webContents) match {
      case Some(map) => map -= key
      case _         =>
    }

  IPCMain.on(
    "store-value",
    (event: IPCMainEvent, key: Any, value: Any) => {
      try {
        storeRawVariable(event.sender, key.asInstanceOf[String], value)
      } catch {
        case e: Throwable => e.printStackTrace()
      }
      event.returnValue = "1"
    }
  )

  IPCMain.on(
    "retrieve-value",
    (event: IPCMainEvent, key: Any) => {
      try {
        event.returnValue = retrieveVariable(event.sender, key.asInstanceOf[String])
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    }
  )

  IPCMain.on(
    "unStore-value",
    (event: IPCMainEvent, key: Any) => {
      try {
        unStoreVariable(event.sender, key.asInstanceOf[String])
      } catch {
        case e: Throwable => e.printStackTrace()
      }
      event.returnValue = "1"
    }
  )

  private val globalVariables: mutable.Map[String, Any] = mutable.Map()

  def storeGlobalVariable(key: String, data: Data): Unit =
    globalVariables += (key -> DataStorage.encode(data))

  // Storing the base directory immediately
  storeGlobalVariable(
    "baseDirectory",
    BaseDirectory(Path.join(MainProcessGlobals.__dirname, "/.."))
  )

  def retrieveGlobalVariable(key: String): Data =
    DataStorage.decode(globalVariables.getOrElse(key, null).asInstanceOf[js.Array[Int]].map(_.toByte))

  private def unStoreGlobalVariable(key: String): Unit =
    globalVariables -= key

  IPCMain.on(
    "store-global-value",
    (event: IPCMainEvent, key: Any, value: Any) => {
      try {
        globalVariables += (key.asInstanceOf[String] -> value)
      } catch {
        case e: Throwable => e.printStackTrace()
      }
      event.returnValue = "1"
    }
  )

  IPCMain.on(
    "retrieve-global-value",
    (event: IPCMainEvent, key: Any) => {
      try {
        event.returnValue = globalVariables.getOrElse(key.toString, null)
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    }
  )

  IPCMain.on(
    "unStore-global-value",
    (event: IPCMainEvent, key: Any) => {
      try {
        unStoreGlobalVariable(key.asInstanceOf[String])
      } catch {
        case e: Throwable => e.printStackTrace()
      }
      event.returnValue = "1"
    }
  )

}
