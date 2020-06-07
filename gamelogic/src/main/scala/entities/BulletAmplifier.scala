package entities

import custommath.Complex
import physics.Polygon

/**
  * A Bullet of the owner's team that passes through a BulletAmplifier sees its radius double, dealing more damage.
  *
  * transformedBullets is the list of the ids of the bullets for which the BulletAmplifier already amplified. It is
  * stored in descending order of ids. The idea is that most often than not, the newly arriving bullet will have an id
  * bigger, so it is much quicker to check if a bullet already has been amplified, and also quicker to add it to the
  * list.
  */
class BulletAmplifier(
    val id: Long,
    val creationTime: Long,
    val ownerId: Long,
    val xPos: Double,
    val yPos: Double,
    val shape: Polygon,
    val rotation: Double,
    transformedBullets: List[Long]
) extends Body {

  def isBulletAmplified(bulletId: Long, idsList: List[Long] = transformedBullets): Boolean =
    // the transformedBullets list will always be sorted from bigger id the smallest
    (!(idsList.isEmpty || idsList.head < bulletId)) && (
      idsList.head == bulletId || isBulletAmplified(bulletId, idsList.tail)
    )
  // if idsList is empty or the first element of idsList is smaller than bulletId, then bulletId can not be in the
  // list, and we return false in that case.
  // Otherwise, we check if bulletId is equal to the head of the list. In that case, of course, bulletId is in the
  // list and we return true.
  // Finally, it just means that bulletId is maybe in the tail of the idsList, so we return the function with the tail

  def addBulletAmplified(bulletId: Long): List[Long] = {
    val splitIds          = transformedBullets.indexWhere(_ < bulletId)
    val (bigger, smaller) = transformedBullets.splitAt(splitIds)
    bigger ++ (bulletId +: smaller)
  }

}

object BulletAmplifier {

  val amplifyingCoefficient: Double = 2.0

  private val length: Double = 100
  private val width: Double  = 20

  private val vertices: Vector[Complex] = Vector(
    Complex(width / 2, -length / 2),
    Complex(width / 2, length / 2),
    Complex(-width / 2, length / 2),
    Complex(-width / 2, -length / 2)
  )

  val bulletAmplifierShape: Polygon = Polygon(vertices, convex = true)

  val lifeTime: Long = 10000

}
