package entities

import scala.collection.mutable

/**
  * Any Game Object is an Entity that extends this.
  */
trait Entity {
  val id: Long

  override def equals(that: Any): Boolean = that match {
    case that: Entity => that.id == this.id
    case _            => false
  }

  override def hashCode: Int = id.hashCode
}

object Entity {
  private var lastId: Long = 0: Long

  private val freedIds: mutable.Queue[Long] = mutable.Queue()

  def newId(): Long =
    if (freedIds.isEmpty) {
      lastId += 1
      lastId
    } else {
      freedIds.dequeue()
    }

  def freeId(id: Long): Unit =
    freedIds.enqueue(id)
}
