package io.cosmicteapot

import java.io.{FileOutputStream, OutputStream}

/**
  * Created by michael on 2/07/2016.
  */
object GridRunner { //             extends App
  println("::: Grid Runner :::")
//  runGrid()
//  runTranslatedGrid()
//  runRotatedGrid()
//  runTriangles()
//  runPeriodLines()
//  runHexes()
//  runLines()
  runScaledHexes()

  def runLines() {
    val lines = Mask.horizontalLine(offsetY = 0.0, thickness = 2.0)
    val masks = for { i <- 0 to 20 } yield {
      Mask.transform(Mat3.rotationMat(Math.Tau * i.asInstanceOf[Double] / 20.0), lines)
    }
    val combinedMask = Mask.or(masks.toArray)
    val translateCombinedMask = Mask.transform(Mat3.translate(-512.0, -512.0), combinedMask)
    val image = ColouredImage.colourize(translateCombinedMask, on = Colour.black, off = Colour.white)
    val out = new FileOutputStream("_rot_lines.png")
    Rasterize.superSamplePng(1024, 1024, 4, out, image)
    out.close()
  }

  def runHexes(): Unit = {
    val mask = Grid.hexMask(64.0, thickness = 5.0)
    val image = ColouredImage.colourize(mask, on = Colour.black, off = Colour.white)
    val out = new FileOutputStream("_hexes.png")
    Rasterize.superSamplePng(1024, 1024, 4, out, image)
    out.close()
  }

  def runScaledHexes() {
    val scales = Array[Double](0.175, 0.25, 0.5, 0.75, 1.0, 2.0, 4.0)

    for ((sc,i) <- scales.zipWithIndex) {
      val mask = Grid.scaledHexMask(64.0, thickness = 1.5, scaleV = sc)
      val image = ColouredImage.colourize(mask, on = Colour.black, off = Colour.white)
      val out = new FileOutputStream(s"_scaled_hexes_$i.png")
      Rasterize.superSamplePng(1024, 1024, 2, out, image)
      out.close()
    }
  }

  def runPeriodLines() {
    val mask = Mask.periodHorizontalLine(offsetY = 0.5, thickness = 0.5, on = 16.0, off = 8.0, startAt = 0.0)
    val modMask = Mask.modulusY(8.0, mask)
    val image = ColouredImage.colourize(modMask, on = Colour.black, off = Colour.white)
    val out = new FileOutputStream("_h_period.png")
    Rasterize.superSamplePng(256, 256, 4, out, image)
    out.close()
  }

  def runTriangles() {
    val mask = Grid.triangleMask(16.0, thickness = 1.0)
    val image = ColouredImage.colourize(mask, on = Colour.black, off = Colour.white)
    val out = new FileOutputStream("_triangles.png")
    Rasterize.superSamplePng(256, 256, 4, out, image)
    out.close()
  }

  def runGrid() {
    val mask = Grid.squareMask(16.0, 16.0, offset = 0.0, thickness = 1.5)
    val image = ColouredImage.colourize(mask, on = Colour.black, off = Colour.white)

    val out = new FileOutputStream("_squares.png")

    Rasterize.superSamplePng(256, 256, 4, out, image)

    out.close()
  }

  def runTranslatedGrid() {
    val mask = Grid.squareMask(16.0, 16.0, offset = 0.0, thickness = 1.0)
    val tMask = Mask.transform(Mat3.translate(8.0, 8.0), mask)
    val image = ColouredImage.colourize(tMask, on = Colour.black, off = Colour.white)

    val out = new FileOutputStream("_translated-squares.png")

    Rasterize.superSamplePng(256, 256, 4, out, image)

    out.close()
  }

  def runRotatedGrid() {
    val mask = Grid.squareMask(16.0, 16.0, offset = 0.5, thickness = 0.5)

    val tMask = Mask.transform(Mat3.rotationMat(Math.Tau / 8.0), mask)
    val image = ColouredImage.colourize(tMask, on = Colour.black, off = Colour.white)

    val out = new FileOutputStream("_rotated-squares.png")

    Rasterize.superSamplePng(256, 256, 4, out, image)

    out.close()
  }
}
