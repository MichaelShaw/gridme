package io.cosmicteapot


import java.awt.image.BufferedImage
import java.io.OutputStream
import java.nio.{ByteOrder, ByteBuffer}

import ar.com.hjg.pngj.{ImageLineHelper, ImageLineInt, PngWriter, ImageInfo}


/**
  * Created by michael on 15/10/2015.
  */

// maybe we keep it in core, as maybe the server needs to recolour images
// or manipulate them in some way ... dynamically stamp on logos etc.

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

  def recolour(img:IntImage, replaceColours:Array[Colour], replacements:Array[Array[Colour]]) : Array[IntImage] = {
    assert(replacements.forall(s => s.length == replaceColours.length))

    val resultImages = Array.fill(replacements.length) { new IntImage(img.width, img.height) }

    val replaceColoursI = replaceColours.map(_.c)
    val replacementsI = replacements.map { cs =>
      cs.map(_.c)
    }

    optimize {
      for {
        i <- 0 until img.pixels.length
      } {
        val c = img.pixels(i)

        var fInd = -1

        for {
          j <- 0 until replaceColoursI.length
        } {
          if(replaceColoursI(j) == c) {
            fInd = j
          }
        }

        if(fInd >= 0) { // reskin :D
          //          println(s"found a pixel to replace!! ${r(c)} ${g(c)} ${b(c)} a ${a(c)}" )
          for(r <- 0 until resultImages.length) {
            resultImages(r).pixels(i) = replacementsI(r)(fInd)
          }
        } else { // don't reskin
          for(r <- 0 until resultImages.length) {
            resultImages(r).pixels(i) = c
          }
        }
      }
    }

    resultImages
  }

  def toBuffer(img:IntImage) : ByteBuffer = {
    import Colour._

    val bb = ByteBuffer.allocateDirect((img.width * img.height) * 4).order(ByteOrder.nativeOrder)
    var i = 0; while (i < img.pixels.length) {
      val p = img.pixels(i)

      bb.put(r(p).asInstanceOf[Byte])
      bb.put(g(p).asInstanceOf[Byte])
      bb.put(b(p).asInstanceOf[Byte])
      bb.put(a(p).asInstanceOf[Byte])

      i += 1
    }
    bb.flip()
    bb
  }

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

  def forBufferedImage(image:BufferedImage) : IntImage = {
    val width = image.getWidth
    val height = image.getHeight

    val raster = image.getRaster

    val img = new IntImage(width, height)

    val sample = new Array[Int](4)

    for {
      x <- 0 until width
      y <- 0 until height
    } {
      raster.getPixel(x, y, sample)
      img.set(x, y, Colour.colourI(sample))
    }

    img
  }

  def toBufferedImage(img:IntImage) : BufferedImage = {
    import Colour.{r, g, b, a}
    val image = new BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_ARGB)
    val raster = image.getRaster

    val sample = new Array[Int](4)

    optimize {
      for {
        x <- 0 until img.width
        y <- 0 until img.height
      } {
        val l = img.location(x, y)
        val c = img.pixels(l)
        sample(0) = r(c)
        sample(1) = g(c)
        sample(2) = b(c)
        sample(3) = a(c)
        raster.setPixel(x, y, sample)
      }
    }

    image
  }
}
