package io.cosmicteapot

import java.io.OutputStream
import ar.com.hjg.pngj.{ImageLineHelper, ImageLineInt, PngWriter, ImageInfo}

/**
  * Created by michael on 15/10/2015.
  */

class IntImage(val width:Int, val height:Int) {
  override def toString = s"IntImage(width:$width, height:$height)"

  val pixels = new Array[Colour.ColourI](width * height)

  @inline
  private def location(x:Int, y:Int) : Int = y * width + x

  def set(x:Int, y:Int, c:Colour.ColourI) {
    pixels(location(x, y)) = c
  }

  def get(x:Int, y:Int) : Colour.ColourI = {
    assert(x < width)
    assert(y < height)

    pixels(location(x, y))
  }

  def assignAll(c:Colour.ColourI) {
    var l = 0; while(l < pixels.length) {
      pixels(l) = c
      l += 1
    }
  }
}

object IntImage {
  import scalaxy.streams.optimize

  def writeToOutputStream(img:IntImage, outputStream:OutputStream) {
    import Colour.{r, g, b, a}

    val imageInfo = new ImageInfo(img.width, img.height, 8, true)
    val png = new PngWriter(outputStream, imageInfo)
    val iline = new ImageLineInt(imageInfo)


    optimize {
      for(row <- 0 until img.height) {
        for(column <- 0 until img.width) {
          val c = img.get(column, row)
          ImageLineHelper.setPixelRGBA8(iline, column, r(c), g(c), b(c), a(c))
        }
        png.writeRow(iline)
      }

    }

    png.end()
  }
}
