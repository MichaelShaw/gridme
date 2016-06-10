package io.cosmicteapot

import io.cosmicteapot.Colour.ColourI

/**
  * Created by michael on 10/06/2016.
  */

object Grid {
  def generate(tilesWide:Int, tilesHigh:Int,
               tileWidth:Int, tileHeight:Int,
               backgroundColour:ColourI, gridColour:ColourI) : IntImage = {
    import scalaxy.streams.optimize

    val width = tilesWide * tileWidth
    val height = tilesHigh * tileHeight

    val image = new IntImage(width, height)
    image.assignAll(backgroundColour)

    // do the griddy stuff here
    optimize {
      for {
        tx <- 0 until tilesWide
      } {
        val x = tx * tileWidth

        for {
          y <- 0 until height
        } {
          image.set(x, y, gridColour)
        }
      }

      for {
        ty <- 0 until tilesHigh
      } {
        val y = ty * tileHeight

        for {
          x <- 0 until width
        } {
          image.set(x, y, gridColour)
        }
      }
    }

    image
  }
}
