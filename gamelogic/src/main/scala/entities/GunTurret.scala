package entities

import physics.Circle

class GunTurret(
    val creationTime: Long,
    val id: Long,
    val ownerId: Long,
    val teamId: Int,
    val xPos: Double,
    val yPos: Double,
    val lastShot: Long,
    val radius: Double,
    val rotation: Double,
    val lifeTotal: Double
) extends Body
    with Living {

  val shape: Circle = new Circle(radius)

}

object GunTurret {

  val shootRate: Long = 200 // shoot every 200 ms.

  val defaultRadius: Double = 15

  val maxLifeTotal: Double = 100

  val defaultReach: Int = 700

}
