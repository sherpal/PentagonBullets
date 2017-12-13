package sharednodejsapis

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport


@js.native
@JSImport("process", JSImport.Namespace)
object NodeProcess extends EventEmitter {
  /**
   * Returns an object giving memory usage statistics about the current process.
   * Note that all statistics are reported in Kilobytes.
   */
  def getProcessMemoryInfo(): ProcessMemoryInfo = js.native

  /**
   * Returns an object giving memory usage statistics about the entire system.
   * Note that all statistics are reported in Kilobytes.
   */
  def getSystemMemoryInfo(): SystemMemoryInfo = js.native

  /**
   * returns an object describing the memory usage of the Node.js process measured in bytes.
   */
  def memoryUsage(): MemoryUsage = js.native
}

trait ProcessMemoryInfo extends js.Object {
  /** The amount of memory currently pinned to actual physical RAM. */
  val workingSetSize: Int
  /** The maximum amount of memory that has ever been pinned to actual physical RAM. */
  val peakWorkingSetSize: Int
  /** The amount of memory not shared by other processes, such as JS heap or HTML content. */
  val privateBytes: Int
  /** The amount of memory shared between processes, typically memory consumed by the Electron code itself. */
  val sharedBytes: Int
}


trait SystemMemoryInfo extends js.Object {
  /** Total amount of physical memory in Kilobytes available to the system. */
  val total: Int
  /** The total amount of memory not being used by applications or disk cache. */
  val free: Int
  /** The total amount of swap memory in Kilobytes available to the system. (Windows and Linux) */
  val swapTotal: Int
  /** The free amount of swap memory in Kilobytes available to the system. (Windows and Linux) */
  val swapFree: Int
}

trait MemoryUsage extends js.Object {
  val rss: Int
  val heapUsed: Int
  val heapTotal: Int
  val heapExternal: Int
}