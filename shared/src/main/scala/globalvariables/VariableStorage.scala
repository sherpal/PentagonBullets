package globalvariables

import sharednodejsapis.IPCRenderer


/**
 * VariableStorage allows to contact the BrowserWindow responsible for storing variables, in order to passe them from
 * pages to pages.
 */
object VariableStorage {

  /**
   * Stores the value under the key identifier for the webContents.
   * @param key         key identifier
   * @param value       value to store
   */
  def storeValue(key: String, value: Any): Unit =
    IPCRenderer.sendSync("store-value", key, value)

  /**
   * Returns a value previously stored under the key identifier.
   * @param key         key identifier under which the value was stored
   * @return            the value previously stored, or null if was not stored before.
   */
  def retrieveValue(key: String): Any =
    IPCRenderer.sendSync("retrieve-value", key)

  /**
   * Removes the value stored under the key identifier.
   */
  def unStoreValue(key: String): Unit =
    IPCRenderer.sendSync("unStore-value", key)


  /**
   * Stores the value for global usage under the key identifier.
   */
  def storeGlobalValue(key: String, value: Any): Unit =
    IPCRenderer.sendSync("store-global-value", key, value)

  /**
   * Retrieve a previously set global variable under the key identifier.
   */
  def retrieveGlobalValue(key: String): Any = IPCRenderer.sendSync("retrieve-global-value", key)

  /**
   * Removes the global value stored under the key identifier.
   */
  def unStoreGlobalValue(key: String): Unit = IPCRenderer.sendSync("unStore-global-value", key)
}
