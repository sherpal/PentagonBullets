package entities

import custommath.Complex
import physics.{Circle, ConvexPolygon, Polygon}

final class LaserLauncher(
                           val id: Long,
                           val xPos: Double,
                           val yPos: Double,
                           val ownerId: Long
                         ) extends Body {

  val rotation: Double = 0.0

  val shape: ConvexPolygon = LaserLauncher.laserLauncherShape

}

object LaserLauncher {

  val laserLauncherShapeRadius: Int = 20

  //val laserLauncherShape: Circle = new Circle(laserLauncherShapeRadius)
  val laserLauncherShape: ConvexPolygon = Polygon(Vector(
    Complex(laserLauncherShapeRadius, laserLauncherShapeRadius),
    Complex(-laserLauncherShapeRadius, laserLauncherShapeRadius),
    Complex(-laserLauncherShapeRadius, -laserLauncherShapeRadius),
    Complex(laserLauncherShapeRadius, -laserLauncherShapeRadius)
  ), convex = true).asInstanceOf[ConvexPolygon]

}
