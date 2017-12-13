package entities

import custommath.Complex
import physics.{Polygon, Shape}

/**
 * A Barrier is like an Obstacle attached to the player, so that its owner and their bullets can go through, but not
 * other entities.
 */
class Barrier(val id: Long, val time: Long, val ownerId: Long, val teamId: Int,
              val xPos: Double, val yPos: Double, val rotation: Double, val shape: Shape) extends Body {

}


object Barrier {

  val lifeTime: Long = 5000

  val length: Double = 75
  val width: Double = 75

  private val vertices: Vector[Complex] = Vector(
    Complex(width / 2, - length / 2), Complex(width / 2, length / 2),
    Complex(- width / 2, length / 2), Complex(- width / 2, - length / 2)
  )

  val shape: Polygon = Polygon(vertices, convex = true)

}
