package io.cosmicteapot

/**
  * Created by michael on 2/07/2016.
  */

object Math {
  private val DegToRad = 0.01745329251994329577
  private val RadToDeg = 57.2957795130823208768

  val Pi = scala.math.Pi
  val Tau = scala.math.Pi * 2

  def radians(degrees: Double) = degrees * DegToRad
  def degrees(radians: Double) = radians * RadToDeg

  def modulusWithoutSign(a:Double, n:Double) : Double = {
    (a % n + n) % n
  }
}

final case class Vec2(x:Double, y:Double)
final case class Vec3(x:Double, y:Double, z:Double) {
  def toVec2 : Vec2 = {
    val xn = x / z
    val yn = y / z
    Vec2(xn, yn)
  }
}

// 2d
final case class Mat3(m00:Double, m10:Double, m20:Double,
                      m01:Double, m11:Double, m21:Double,
                      m02:Double, m12:Double, m22:Double) {
  def *(m: Mat3) = Mat3(
    m00*m.m00 + m01*m.m10 + m02*m.m20,
    m10*m.m00 + m11*m.m10 + m12*m.m20,
    m20*m.m00 + m21*m.m10 + m22*m.m20,

    m00*m.m01 + m01*m.m11 + m02*m.m21,
    m10*m.m01 + m11*m.m11 + m12*m.m21,
    m20*m.m01 + m21*m.m11 + m22*m.m21,

    m00*m.m02 + m01*m.m12 + m02*m.m22,
    m10*m.m02 + m11*m.m12 + m12*m.m22,
    m20*m.m02 + m21*m.m12 + m22*m.m22
  )

  def *(u: Vec3) = new Vec3(
    m00*u.x + m01*u.y + m02*u.z,
    m10*u.x + m11*u.y + m12*u.z,
    m20*u.x + m21*u.y + m22*u.z
  )
}

object Mat3 {
  val identity = Mat3(
    1.0, 0.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 0.0, 1.0
  )

  def scale(x:Double, y:Double) : Mat3 = {
    Mat3(
      x, 0.0, 0.0,
      0.0, y, 0.0,
      0.0, 0.0, 1.0
    )
  }

  def translate(x:Double, y:Double) : Mat3 = {
    Mat3(
      1.0, 0.0, 0.0,
      0.0, 1.0, 0.0,
      x,   y,   1.0
    )
  }

  def rotationMat(angle: Double) : Mat3 = {
    import scala.math.{cos, sin}

    val cosA = cos(angle)
    val sinA = sin(angle)

    Mat3(
      cosA,  sinA, 0,
      -sinA, cosA, 0,
      0,     0,    1.0
    )
  }
}