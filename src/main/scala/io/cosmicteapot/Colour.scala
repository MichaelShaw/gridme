package io.cosmicteapot


import java.nio.ByteOrder

/**
  * Created by michael on 15/10/2015.
  */

object Colour {
  def r(c:ColourI) : Int = c & 0xff
  def g(c:ColourI) : Int = (c >>> 8) & 0xff
  def b(c:ColourI) : Int = (c >>> 16) & 0xff
  def a(c:ColourI) : Int = (c >>> 24) & 0xff

  def rf(c:ColourI) : Float = r(c).asInstanceOf[Float] / 255f
  def gf(c:ColourI) : Float = g(c).asInstanceOf[Float] / 255f
  def bf(c:ColourI) : Float = b(c).asInstanceOf[Float] / 255f
  def af(c:ColourI) : Float = a(c).asInstanceOf[Float] / 255f

  type ColourI = Int

  val maskOffOneBit = ~ Integer.parseInt("00000001000000000000000000000000", 2)

  def littleEndian : Boolean = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
  def colourI(sample:Array[Int]) : ColourI = colourI(sample(0), sample(1), sample(2), sample(3))
  def colourI(r:Int, g:Int, b:Int, a:Int) : ColourI = {
    val ur = clamp(r, 0, 255)
    val ug = clamp(g, 0, 255)
    val ub = clamp(b, 0, 255)
    val ua = clamp(a, 0, 255)
    if(littleEndian) {
      //      println("we are little endian")
      (ua << 24 | ub << 16 | ug << 8 | ur) & maskOffOneBit // mask off one bit just
    } else {
      //      println("we are not little endian!")
      ur << 24 | ug << 16 | ub << 8 | ua
    }
  }
  def clamp(n:Int, a:Int, b:Int) : Int = {
    if(n < a) a else if(n > b ) b else n
  }
}
