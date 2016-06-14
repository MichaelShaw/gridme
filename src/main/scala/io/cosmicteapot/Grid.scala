package io.cosmicteapot

import java.io.OutputStream

import ar.com.hjg.pngj.{ImageInfo, ImageLineHelper, ImageLineInt, PngWriter}
import io.cosmicteapot.Colour.ColourI

import scalaxy.streams._

/**
  * Created by michael on 10/06/2016.
  */


object Grid {
  type Image = (Int, Int) => ColourI

  def verticalLines(everyX:Int, on:ColourI, off:ColourI = Colour.transWhite) : Image = { (x, y) =>
    if ((x % everyX) == 0) {
      on
    } else {
      off
    }
  }

  def horizontalLines(everyY:Int, on:ColourI, off:ColourI = Colour.transWhite) : Image = { (x, y) =>
    if ((y % everyY) == 0) {
      on
    } else {
      off
    }
  }

  def grid(everyX:Int, everyY:Int, on:ColourI, off:ColourI = Colour.transWhite) : Image = { (x, y) =>
    if ((x % everyX) == 0 || (y % everyY) == 0) {
      on
    } else {
      off
    }
  }

  def rasterizePNG(width:Int, height:Int, outputStream:OutputStream, image:Image) {
    import Colour.{r, g, b, a}

    val imageInfo = new ImageInfo(width, height, 8, true)
    val png = new PngWriter(outputStream, imageInfo)
    val iline = new ImageLineInt(imageInfo)

    optimize {
      for(y <- 0 until height) {
        for(x <- 0 until width) {
          val c = image(x, y)
          ImageLineHelper.setPixelRGBA8(iline, x, r(c), g(c), b(c), a(c))
        }
        png.writeRow(iline)
      }
    }

    png.end()
  }
}



