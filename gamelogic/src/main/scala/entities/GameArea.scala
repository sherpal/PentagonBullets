package entities

import custommath.Complex
import custommath.Complex.i
import gamestate.{ActionSource, GameAction, GameState}
import gamestate.actions.NewObstacle
import physics.ConvexPolygon
import time.Time

class GameArea(val width: Int = 1000, val height: Int = 800) {

  def randomPos(): (Double, Double) = (
    (scala.util.Random.nextInt(width - 100) - width / 2 + 50).toDouble,
    (scala.util.Random.nextInt(height - 100) - height / 2 + 50).toDouble
  )

  /**
    * Returns a random position in the region with specified center and dimensions.
    */
  def randomPos(center: Complex, width: Double, height: Double): (Double, Double) = (
    scala.util.Random.nextInt(width.toInt) + center.re - width / 2,
    scala.util.Random.nextInt(height.toInt) + center.im - height / 2
  )

  def randomPos(mist: Mist): (Double, Double) = {
    val width  = mist.sideLength.toInt
    val height = mist.sideLength.toInt
    (
      (scala.util.Random.nextInt(width - 100) - width / 2 + 50).toDouble,
      (scala.util.Random.nextInt(height - 100) - height / 2 + 50).toDouble
    )
  }
  val topEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(0, height / 2 + 5),
    Obstacle.segmentObstacleVertices(-width / 2 - 10, width / 2 + 10, 10)
//    Vector(
//      Complex(-width / 2 - 10, -5), Complex(width / 2 + 10, -5),
//      Complex(width / 2 + 10, 5), Complex(- width / 2 - 10, 5)
//    )
  )

  val bottomEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(0, -height / 2 - 5),
    Obstacle.segmentObstacleVertices(-width / 2 - 10, width / 2 + 10, 10)
//    Vector(
//      Complex(-width / 2 - 10, -5), Complex(width / 2 + 10, -5),
//      Complex(width / 2 + 10, 5), Complex(- width / 2 - 10, 5)
//    )
  )

  val leftEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(-width / 2 - 5, 0),
    Obstacle.segmentObstacleVertices(-i * height / 2, i * height / 2, 10)
//    Vector(
//      Complex(-5, -height / 2), Complex(5, -height / 2),
//      Complex(5, height / 2), Complex(-5, height / 2)
//    )
  )

  val rightEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(width / 2 + 5, 0),
    Obstacle.segmentObstacleVertices(-i * height / 2, i * height / 2, 10)
//    Vector(
//      Complex(-5, -height / 2), Complex(5, -height / 2),
//      Complex(5, height / 2), Complex(-5, height / 2)
//    )
  )

  val gameAreaEdgesVertices: List[(Complex, Vector[Complex])] = List(
    topEdgeVertices,
    bottomEdgeVertices,
    leftEdgeVertices,
    rightEdgeVertices
  )

  def createCenterSquare(radius: Double, source: ActionSource): NewObstacle = {
    val vertices = (0 to 3).map(j => Complex.rotation(j * math.Pi / 2)).map(_ * radius).toVector

    NewObstacle(GameAction.newId(), Time.getTime, Entity.newId(), Complex(0, 0), vertices, source)
  }

  def createObstacle(gameState: GameState, width: Int, height: Int, source: ActionSource): NewObstacle = {
    val (x, y) = randomPos()
    val vertices = Vector(
      Complex(-width / 2, -height / 2),
      Complex(width / 2, -height / 2),
      Complex(width / 2, height / 2),
      Complex(-width / 2, height / 2)
    )

    val obstacleShape: ConvexPolygon = new ConvexPolygon(vertices)

    if (gameState.players.values.exists(
          player => player.shape.collides(player.pos, player.rotation, obstacleShape, Complex(x, y), 0)
        ))
      createObstacle(gameState, width, height, source)
    else
      NewObstacle(GameAction.newId(), Time.getTime, Entity.newId(), Complex(x, y), vertices, source)

  }

}

object GameArea {

  def sizeFromNbrPlayers(nbrPlayers: Int): Int = 1000 * (nbrPlayers - 1)

  def apply(nbrPlayers: Int): GameArea = {
    val size = sizeFromNbrPlayers(nbrPlayers)
    new GameArea(size, size)
  }

}
