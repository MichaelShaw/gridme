package io.cosmicteapot

import java.io.OutputStream
import ar.com.hjg.pngj.{ImageInfo, ImageLineHelper, ImageLineInt, PngWriter}
import io.cosmicteapot.Colour.ColourI

import scalaxy.streams._

/**
  * Created by michael on 10/06/2016.
  */

object Mask {
  type Mask = (Double, Double) => Boolean

  import scala.math.abs

  def periodHorizontalLine(offsetY:Double, thickness:Double, on:Double, off:Double, startAt:Double) : Mask = {
    val period = on + off
    val m:Mask = { (x:Double, y:Double) =>
      val withinPeriod = Math.modulusWithoutSign(x + startAt, period) < on
      if (withinPeriod) {
        abs(y - offsetY) <= thickness
      } else {
        false
      }
    }
    m
  }

  def horizontalLine(offsetY:Double, thickness:Double) : Mask = { (x, y) =>
    abs(y - offsetY) <= thickness
  }

  def verticalLine(offsetX:Double, thickness:Double) : Mask = { (x, y) =>
    abs(x - offsetX) <= thickness
  }

  def modulusY(yPeriod:Double, mask:Mask) : Mask = {
    val halfPeriod = yPeriod / 2.0
    val m:Mask = { (x, y) =>
      mask(x, Math.modulusWithoutSign(y + halfPeriod, yPeriod) - halfPeriod)
    }
    m
  }

  def modulusX(xPeriod:Double, mask:Mask) : Mask = {
    val halfPeriod = xPeriod / 2.0
    val m:Mask = { (x, y) =>
      mask(Math.modulusWithoutSign(x + halfPeriod, xPeriod) - halfPeriod, y)
    }
    m
  }

  def or(l:Mask, r:Mask) : Mask = { (x,y) =>
    l(x, y) || r(x, y)
  }

  def or(masks:Array[Mask]) : Mask = { (x, y) =>
    optimize {
      masks.exists { m => m(x, y) }
    }
  }

  def transform(mat:Mat3, mask:Mask) : Mask = { (x, y) =>
    val v = Vec3(x, y, 1.0)
    val tv = mat * v
    val v2 = tv.toVec2
    mask(v2.x, v2.y)
  }
}

object ColouredImage {
  type ColouredImage = (Double, Double) => ColourI

  def colourize(mask:Mask.Mask, on:ColourI, off:ColourI) : ColouredImage = { (x, y) =>
    if (mask(x, y)) {
      on
    } else {
      off
    }
  }
}

object Rasterize {
  def superSamplePng(width:Int, height:Int, samples:Int, outputStream:OutputStream, image:ColouredImage.ColouredImage): Unit = {
    import Colour.{r, g, b, a}

    val imageInfo = new ImageInfo(width, height, 8, true)
    val png = new PngWriter(outputStream, imageInfo)
    val iline = new ImageLineInt(imageInfo)

    val perSample = 1.0 / samples
    val sampleStart = perSample * 0.5

    val totalSamplesPerPixel = samples * samples

    optimize {
      for (y <- 0 until height) {
        for (x <- 0 until width) {
          // super sampling
          var rTotal = 0
          var gTotal = 0
          var bTotal = 0
          var aTotal = 0

          for {
            ys <- 0 until samples
          } {
            val ySample = y + sampleStart + ys * perSample
            for {
              xs <- 0 until samples
            } {
              val xSample = x + sampleStart + xs * perSample
              val c = image(xSample, ySample)
              rTotal += r(c)
              gTotal += g(c)
              bTotal += b(c)
              aTotal += a(c)
            }
          }

          ImageLineHelper.setPixelRGBA8(iline, x,
            rTotal / totalSamplesPerPixel,
            gTotal / totalSamplesPerPixel,
            bTotal / totalSamplesPerPixel,
            aTotal / totalSamplesPerPixel
          )
        }
        png.writeRow(iline)
      }
    }
    png.end()
  }

  def png(width:Int, height:Int, outputStream:OutputStream, image:ColouredImage.ColouredImage) {
    import Colour.{r, g, b, a}

    val imageInfo = new ImageInfo(width, height, 8, true)
    val png = new PngWriter(outputStream, imageInfo)
    val iline = new ImageLineInt(imageInfo)

    optimize {
      for(y <- 0 until height) {
        for(x <- 0 until width) {
          // sample centre of pixel
          val xSample = x + 0.5
          val ySample = y + 0.5

          val c = image(xSample, ySample)
          ImageLineHelper.setPixelRGBA8(iline, x, r(c), g(c), b(c), a(c))
        }
        png.writeRow(iline)
      }
    }

    png.end()
  }
}



