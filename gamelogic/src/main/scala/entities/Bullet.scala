package entities


/**
 * A Player represents a Human player.
 */
class Bullet(val id: Long, val time: Long, val ownerId: Long, val teamId: Int, val xPos: Double, val yPos: Double,
             val radius: Int, val direction: Double, val speed: Double, val travelledDistance: Double)
  extends BulletLike {

}

object Bullet {
  val defaultRadius: Int = 4

  val speed: Int = 400

  val reach: Int = 1000

  val damage: Double = 10

  val reloadTime: Long = 1000 / 11

  def damageFromRadius(radius: Int): Double = radius * 10 / 4.0
}
