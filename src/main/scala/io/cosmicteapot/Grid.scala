package io.cosmicteapot

import io.cosmicteapot.Colour.ColourI
import io.cosmicteapot.Mask.Mask

/**
  * Created by michael on 2/07/2016.
  */
object Grid {
  def squareMask(tileWidth:Double, tileHeight:Double, offset:Double, thickness:Double) : Mask = {
    val horizontalLines = Mask.modulusY(tileWidth, Mask.horizontalLine(offsetY = offset, thickness = thickness))
    val verticalLines = Mask.modulusX(tileHeight, Mask.verticalLine(offsetX = offset, thickness = thickness))
    Mask.or(horizontalLines, verticalLines)
  }

  def triangleMask(width:Double, thickness:Double) : Mask = {
    val horizontalLines = Mask.modulusY(width, Mask.horizontalLine(offsetY = 0.0, thickness = thickness))
    val linesB = Mask.transform(Mat3.rotationMat(Math.Tau / 6), horizontalLines)
    val linesC = Mask.transform(Mat3.rotationMat(Math.Tau * 2 / 6), horizontalLines)

    Mask.or(Array(horizontalLines, linesB, linesC))
  }

  def hexMask(sideLength:Double, thickness:Double) : Mask = {
    val hexHeight = sideLength * scala.math.cos(Math.radians(30)) * 2.0

    val mask = Mask.periodHorizontalLine(offsetY = 0.0, thickness = thickness, on = sideLength, off = sideLength * 2.0, startAt = 0.0) // 1 on, 2 off
    val tiledMask = Mask.modulusY(hexHeight, mask)

    val masks = (for {
      i <- 0 until 3
    } yield {
      val angle = Math.Tau * (i / 3.0)
      val rotation = Mat3.rotationMat(angle)
      val translate = Mat3.translate(sideLength * 1.5, hexHeight / 2.0)
      val transform = translate * rotation

      val rotatedMask = Mask.transform(rotation, tiledMask)
      val companionMask = Mask.transform(transform, tiledMask)

      Array(rotatedMask, companionMask)
    }).flatten.toArray


//    val m = Mask.or(masks)
//    val sc = Mat3.scale(12.0, 12.0)
//    Mask.transform(sc, m)
    Mask.or(masks)
  }
}
