package entities


class SmashBullet(
                 val id: Long, val time: Long, val ownerId: Long, val xPos: Double, val yPos: Double,
                 val radius: Int, val direction: Double, val speed: Double, val travelledDistance: Double,
                 val lastGrow: Long
                 ) extends BulletLike {

}

object SmashBullet {

  val defaultRadius: Int = 5 * Bullet.defaultRadius

  val endRadius: Int = 3 * defaultRadius

  val reach: Int = Bullet.reach / 2

  val speed: Double = Bullet.speed

  val lifeTime: Long = math.round(1000 * reach / speed)

  val growRate: Long = lifeTime / 4

  val growValue: Int = (endRadius - defaultRadius) / 4


}
