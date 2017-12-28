package entities

import physics.Circle

final class LaserLauncher(
                           val id: Long,
                           val xPos: Double,
                           val yPos: Double,
                           val ownerId: Long
                         ) extends Body {

  val rotation: Double = 0.0

  val shape: Circle = LaserLauncher.laserLauncherShape

}

object LaserLauncher {

  val laserLauncherShapeRadius: Int = 10

  val laserLauncherShape: Circle = new Circle(laserLauncherShapeRadius)

}
