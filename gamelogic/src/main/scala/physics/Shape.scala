/**
 * License
 * =======
 *
 * The MIT License (MIT)
 *
 *
 * Copyright (c) 2017 Antoine DOERAENE @sherpal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package physics

import custommath.Complex
import scala.Ordering.Double.TotalOrdering


abstract sealed class Shape {
  val boundingBox: BoundingBox

  def collides(thisTranslation: Complex, thisRotation: Double,
               that: Shape, thatTranslation: Complex, thatRotation: Double): Boolean

  def intersectSegment(translation: Complex, rotation: Double, z1: Complex, z2: Complex): Boolean
}

abstract sealed class Polygon extends Shape {
  val vertices: Vector[Complex]
  val triangulation: List[Triangle]


  def collides(thisTranslation: Complex, thisRotation: Double,
               that: Shape, thatTranslation: Complex, thatRotation: Double): Boolean = {
    if (!boundingBox.intersect(that.boundingBox, thisTranslation, thatTranslation)) false
    else {
      that match {
        case that: Polygon =>
          this.triangulation.exists(t => that.triangulation.exists(thatT => {
            t.overlap(thatT, thisRotation, thisTranslation, thatRotation, thatTranslation)
          }))
        case that: Circle =>
          this.triangulation.exists(t => t.overlapDisk(
            that, thatTranslation, thisRotation, thisTranslation
          ))
      }
    }
  }

  val boundingBox: BoundingBox = {
    val center = vertices.sum / vertices.length
    val radius = math.sqrt(vertices.map(z => (z - center).modulus2).max)

    new BoundingBox(center.re - radius, center.im - radius, center.re + radius, center.im + radius)
  }

  def intersectSegment(translation: Complex, rotation: Double, z1: Complex, z2: Complex): Boolean =
    triangulation.exists(triangle => {
      val actualVertices = triangle.vertices.map(_ * Complex.rotation(rotation) + translation)
      triangle.contains(z1.re, z1.im, rotation, translation) ||
        triangle.contains(z2.re, z2.im, rotation, translation) ||
        actualVertices.zip(actualVertices.tail :+ actualVertices.head).exists({case (w1, w2) =>
          Shape.intersectingSegments(w1.re, w1.im, w2.re, w2.im, z1.re, z1.im, z2.re, z2.im)
      })
    })

}

object Polygon {
  def apply(vertices: Vector[Complex], convex: Boolean = false): Polygon =
    if (convex) new ConvexPolygon(vertices) else new NonConvexPolygon(vertices)
}


class NonConvexPolygon(val vertices: Vector[Complex]) extends Polygon {
  val triangulation: List[Triangle] = Shape.earClipping(vertices)
}

class MonotonePolygon(val vertices: Vector[Complex]) extends Polygon {
  val triangulation: List[Triangle] = Shape.triangulateMonotonePolygon(vertices)
}

class ConvexPolygon(val vertices: Vector[Complex]) extends Polygon {
  if (scala.scalajs.LinkingInfo.developmentMode) {
    // checking if vertices are counterclockwise
    val edges = ((vertices.last, vertices(0)) +: vertices.zip(vertices.tail)).map(elem => elem._1 - elem._2)
    assert(edges.zip(edges.tail).forall(elem => elem._1.crossProduct(elem._2) > 0), vertices.mkString(", "))
  }

  val triangulation: List[Triangle] = {
    (for (j <- 1 until vertices.length - 1) yield Triangle(vertices(0), vertices(j), vertices(j + 1))).toList
  }
}


abstract sealed class Curved extends Shape


class Circle(val radius: Double) extends Curved {
  //val triangulation: List[Triangle] = Shape.regularPolygon(math.max((radius / 2).toInt, 8), radius).triangulation

  val boundingBox: BoundingBox = new BoundingBox(- radius, - radius, radius, radius)

  def intersectSegment(translation: Complex, x0: Double, y0: Double, x1: Double, y1: Double): Boolean = {
    val vx = x1 - x0
    val vy = y1 - y0
    val zx = x0 - translation.re
    val zy = y0 - translation.im

    // first checking in one edge is in the disk
    zx * zx + zy * zy <= radius * radius || {
      // then checking if there is an intersection
      val intersectionPoints = Shape.solveSecondDegree(
        vx * vx + vy * vy,
        2 * (zx * vx + zy * vy),
        zx * zx + zy * zy - radius * radius
      )

      intersectionPoints match {
        case None =>
          // no intersection between the line and the disk
          false
        case Some((lambda1, lambda2)) =>
          // intersection between the line and the disk, checking if it is in the segment
          (0 <= lambda1 && lambda1 <= 1) || (0 <= lambda2 && lambda2 <= 1)
      }
    }
  }

  def overlapTriangle(t: Triangle): Boolean = {
    t.contains(0, 0) || intersectSegment(t.x0, t.y0, t.x1, t.y1) ||
    intersectSegment(t.x0, t.y0, t.x2, t.y2) || intersectSegment(t.x1, t.y1, t.x2, t.y2)
  }

  def collides(thisTranslation: Complex, thisRotation: Double,
               that: Shape, thatTranslation: Complex, thatRotation: Double): Boolean = that match {
    case that: Circle =>
      val dx = thisTranslation.re - thatTranslation.re
      val dy = thisTranslation.im - thatTranslation.im
      dy * dy + dx * dx <= (radius + that.radius) * (radius + that.radius)
    case that: Polygon => that.collides(thatTranslation, thatRotation, this, thisTranslation, thisRotation)
  }

  def intersectSegment(translation: Complex, rotation: Double, z1: Complex, z2: Complex): Boolean = {
    val dir = z2 - z1
    val dirMod = dir.modulus
    val unitVec = dir / dirMod

    val scalarProduct = (translation - z1) scalarProduct unitVec

    0 <= scalarProduct && scalarProduct <= dirMod && {
      val projection = z1 + unitVec * scalarProduct
      val orthogonalProjection = translation - projection
      orthogonalProjection.modulus2 < radius * radius
    }

  }

}

//class Ellipse(val xRadius: Double, val yRadius: Double) extends Curved {
//  val boundingBox: BoundingBox = new BoundingBox(centerX - xRadius, centerY - yRadius, centerX + xRadius, centerY + yRadius)
//}



class Triangle(val x0: Double, val y0: Double,
               val x1: Double, val y1: Double,
               val x2: Double, val y2: Double) {

  val vertices: Vector[Complex] = Vector(Complex(x0, y0), Complex(x1, y1), Complex(x2, y2))

  val det: Double = Complex(x1 - x0, y1 - y0) crossProduct Complex(x2 - x0, y2 - y0)

  if (scala.scalajs.LinkingInfo.developmentMode) {
    assert(det > 0, "triangle is not counterclockwise oriented")
  }

  def overlapDisk(that: Circle, center: Complex, rot: Double = 0, translation: Complex = 0): Boolean = {
    val transformedVertices = vertices.map(_ * Complex.rotation(rot) + translation)
    that.intersectSegment(
      center, transformedVertices(0).re, transformedVertices(0).im,
      transformedVertices(1).re, transformedVertices(1).im
    ) ||
    that.intersectSegment(
      center, transformedVertices(1).re, transformedVertices(1).im,
      transformedVertices(2).re, transformedVertices(2).im
    ) ||
    that.intersectSegment(
      center, transformedVertices(2).re, transformedVertices(2).im,
      transformedVertices(0).re, transformedVertices(0).im
    )
  }

  def overlap(that: Triangle, rot: Double = 0, trans: Complex = 0,
              rotThat: Double = 0, transThat: Complex = 0): Boolean = {
    val cs = effectiveCoordinates(rot, trans)
    val csThat = that.effectiveCoordinates(rotThat, transThat)

    Shape.intersectingSegments(cs(0).re, cs(0).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(2).re, cs(2).im,
        csThat(0).re, csThat(0).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(2).re, cs(2).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(2).re, csThat(2).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(2).re, cs(2).im,
        csThat(0).re, csThat(0).im, csThat(2).re, csThat(2).im) ||
      Shape.intersectingSegments(cs(2).re, cs(2).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(2).re, csThat(2).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(1).re, cs(1).im,
        csThat(2).re, csThat(2).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(2).re, cs(2).im,
        csThat(2).re, csThat(2).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(2).re, cs(2).im, cs(1).re, cs(1).im,
        csThat(2).re, csThat(2).im, csThat(1).re, csThat(1).im) ||
      this.contains(csThat(0).re, csThat(0).im, cs) ||
      this.contains(csThat(1).re, csThat(1).im, cs) ||
      this.contains(csThat(2).re, csThat(2).im, cs) ||
      that.contains(cs(0).re, cs(0).im, csThat) ||
      that.contains(cs(1).re, cs(1).im, csThat) ||
      that.contains(cs(2).re, cs(2).im, csThat)
  }

  def effectiveCoordinates(rotation: Double, translation: Complex): Vector[Complex] =
    vertices.map(z => z * Complex.rotation(rotation) + translation)

  def contains(x: Double, y: Double, rotation: Double = 0, translation: Complex  = 0): Boolean = {
    val cs = if (rotation == 0 && translation == Complex(0,0)) vertices else
      effectiveCoordinates(rotation, translation)
    contains(x, y, cs)
  }

  def contains(x: Double, y: Double, cs: Vector[Complex]): Boolean = {
    val coef1 = (cs(2).im - cs(0).im) * (x - cs(0).re) - (cs(2).re - cs(0).re) * (y - cs(0).im)
    val coef2 = (cs(1).re - cs(0).re) * (y - cs(0).im) - (cs(1).im - cs(0).im) * (x - cs(0).re)

    coef1 >= 0 && coef2 >= 0 && coef1 + coef2 <= det
  }
}

object Triangle {
  def apply(v0: Complex, v1: Complex, v2: Complex): Triangle = new Triangle(
    v0.re, v0.im, v1.re, v1.im, v2.re, v2.im
  )
}


class BoundingBox(val left: Double, val bottom: Double, val right: Double, val top: Double) {
  def intersect(that: BoundingBox, translation: Complex = 0, translationThat: Complex = 0): Boolean = {
    math.max(this.left + translation.re, that.left + translationThat.re) <
      math.min(this.right + translation.re, that.right + translationThat.re) &&
      math.min(this.top + translation.im, that.top + translationThat.im) >
        math.max(this.bottom + translation.im, that.bottom + translationThat.im)
  }
}



object Shape {

  def segmentKind(z0: Complex, z1: Complex, z2: Complex): Int = {
    val det = (z1 - z0) crossProduct(z2 - z1)

    if (det > 0) { // convex corner
      if (z0.im < z1.im && z2.im < z1.im) 1 // no need of horizontal segment
      else if (z0.im > z1.im && z2.im > z1.im) 2 // full horizontal segment
      else if (z0.im < z1.im && z2.im > z1.im) 3 // horizontal segment to the left
      else 4 // horizontal segment to the right
    } else { // concave corner
      if (z0.im > z1.im && z2.im > z1.im) 1
      else if (z0.im < z1.im && z2.im < z1.im) 2
      else if (z0.im > z1.im && z2.im < z1.im) 3
      else 4
    }
  }

  def triangulateMonotonePolygon(vertices: Vector[Complex]): List[Triangle] = {
    def triangulationAcc(vertices: Vector[Complex], acc: List[Triangle]): List[Triangle] = {
      if (vertices.length == 3) Triangle(vertices.head, vertices(1), vertices.last) :: acc
      else {
        val convexCorner = vertices.indices.find(j => {
          val prev = if (j > 0) j - 1 else vertices.length - 1
          val next = if (j < vertices.length - 1) j + 1 else 0
          ((vertices(j) - vertices(prev)) crossProduct (vertices(next) - vertices(j))) > 0
        })


        convexCorner match {
          case None =>
            Console.err.print("Polygon was not Monotone")
            List()
          case Some(idx) =>
            val prev = if (idx > 0) idx - 1 else vertices.length - 1
            val next = if (idx < vertices.length - 1) idx + 1 else 0
            triangulationAcc(
              vertices.take(idx) ++ vertices.drop(idx + 1),
              Triangle(vertices(prev), vertices(idx), vertices(next)) :: acc
            )
        }
      }
    }

    triangulationAcc(vertices, List[Triangle]())
  }

  private class Corner(val z: Complex, val next: Complex, val prev: Complex, val cornerIndex: Int) {
    val det: Double = (z - prev).crossProduct(next - z)
    def triangle: Triangle = Triangle(prev, z, next)
    def angle: Double = {
      val arg1 = (prev - z).arg
      val arg2 = (next - z).arg
      (if (arg1 < 0) arg1 + 2 * math.Pi else arg1) - (if (arg2 < 0) arg2 + 2 * math.Pi else arg2)
    }
    def isVertex(v: Complex): Boolean = v == z || v == next || v == prev
  }

  def earClipping(vertices: Vector[Complex]): List[Triangle] = {
    def earClippingAcc(vs: Vector[Complex], acc: List[Triangle]): List[Triangle] = {
      if (vs.length == 3) Triangle(vs.head, vs(1), vs.last) :: acc
      else {

        val corners = for (j <- vs.indices) yield new Corner(
          vs(j), vs(if (j == vs.length - 1) 0 else j + 1), vs(if (j == 0) vs.length - 1 else j-1), j
        )

        val (convex, reflex) = corners.partition(_.det > 0)
        val ears = convex.filter(v => !reflex.exists(c => (!v.isVertex(c.z)) && v.triangle.contains(c.z.re, c.z.im)))
        val process = ears.minBy(_.angle)

        val (beforeCorner, afterCorner) = vs.splitAt(process.cornerIndex)

        earClippingAcc(beforeCorner ++ afterCorner.tail, process.triangle :: acc)
      }
    }

    earClippingAcc(vertices, List()).sortWith((t1, t2) => t1.det > t2.det)
  }



  def regularPolygon(nbrSides: Int, radius: Double = 1): ConvexPolygon = new ConvexPolygon(
    (0 until nbrSides).map(j => radius * Complex.exp(Complex.i * 2 * math.Pi * j / nbrSides)).toVector)


  def intersectionPoint(x11: Double, y11: Double, x12: Double, y12: Double,
                        x21: Double, y21: Double, x22: Double, y22: Double): Option[Complex] = {
    val v1x = x12 - x11
    val v1y = y12 - y11
    val v2x = x22 - x21
    val v2y = y22 - y21

    val det = -v1x * v2y + v2x * v1y

    if (math.abs(det) < 1e-6) None
    else {
      val coef1 = (x11 - x21) * v2y - (y11 - y21) * v2x
      val coef2 = (x11 - x21) * v1y - (y11 - y21) * v1x

      if ((det >= 0 && coef1 <= det && coef2 <= det && coef1 >= 0 && coef2 >= 0) ||
        (coef1 <= 0 && coef2 <= 0 && coef1 >= det && coef2 >= det)) {
        Some(Complex(x11, y11) + coef1 / det * Complex(x12 - x11, y12 - y11))
      } else None
    }
  }

  def intersectingSegments(x11: Double, y11: Double, x12: Double, y12: Double,
                           x21: Double, y21: Double, x22: Double, y22: Double): Boolean = {

    val v1x = x12 - x11
    val v1y = y12 - y11
    val v2x = x22 - x21
    val v2y = y22 - y21

    val det = -v1x * v2y + v2x * v1y

    val coef1 = (x11 - x21) * v2y - (y11 - y21) * v2x
    val coef2 = (x11 - x21) * v1y - (y11 - y21) * v1x

    if (det >= 0) coef1 <= det && coef2 <= det && coef1 >= 0 && coef2 >= 0
    else coef1 <= 0 && coef2 <= 0 && coef1 >= det && coef2 >= det
  }

  def solveSecondDegree(a: Double, b: Double, c: Double): Option[(Double, Double)] = {
    val rho = b * b - 4 * a * c

    if (rho < 0) None
    else Some(((- b + math.sqrt(rho)) / (2 * a), (- b - math.sqrt(rho)) / (2 * a)))
  }

}

