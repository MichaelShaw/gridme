package io.cosmicteapot

import java.io.ByteArrayOutputStream

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.scalatra._
import org.scalatra.LifeCycle
import javax.servlet.ServletContext

import fastparse.all._

import io.cosmicteapot.Colour.ColourI

import scalaz.ValidationNel

object JettyLauncher extends App { //
  // this is my entry object as specified in sbt project definition
  run()
  def run() {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")
    context.setInitParameter(ScalatraListener.LifeCycleKey, "io.cosmicteapot.ScalatraBootstrap")

    server.setHandler(context)

    server.start()
    server.join()
  }
}

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context mount (new GridServlet, "/*")
  }
}

class GridServlet extends MyScalatraWebAppStack {
  def time() : Double = {
    System.currentTimeMillis() / 1000.0
  }

  get("/") {
    contentType="text/html"
//    jade("/index", "layout" -> "WEB-INF/templates/layouts/default.jade")
    jade("index")
  }

  get("/squares/new") {
    contentType="text/html"
    jade("/squares", "layout" -> "WEB-INF/templates/layouts/default.jade")
  }

  get("/squares.png") {
    import scalaz._
    import Scalaz._
    import scalaz.{Failure, Success, Validation, ValidationNel}

    val widthInTiles = toInt(params("width_tiles"), "Width Tiles")
    val heightInTiles = toInt(params("height_tiles"), "Height Tiles")
    val tileWidth = toInt(params("tile_width"), "Tile Width")
    val tileHeight = toInt(params("tile_height"), "Tile Height")

    val backgroundColour = parseColour(params("background_colour"), "Background Colour")
    val gridColour = parseColour(params("grid_colour"), "Grid Colour")

    val gridValidation = (widthInTiles |@| heightInTiles |@| tileWidth |@| tileHeight |@| backgroundColour |@| gridColour) {
      (w, h, tw, th, backgroundColour, gridColour) =>

      val width = w * tw
      val height = h * th

      val mask = Grid.squareMask(tw, th, offset = 0.5, thickness = 0.5)
      val image = ColouredImage.colourize(mask, on = gridColour, off = backgroundColour)

      Rasterize.png(width, height, response.getOutputStream, image)

      ()
    }

    gridValidation match {
      case Success(()) =>
        contentType="image/png"
        ()
      case Failure(errors) =>
        println(s"Errors -> $errors")
        errors.toString()
    }
  }

  get("/hexes/new") {
    contentType="text/html"
    jade("/hexes", "layout" -> "WEB-INF/templates/layouts/default.jade")
  }

  get("/hexes.png") {
    import scalaz._
    import Scalaz._
    import scalaz.{Failure, Success, Validation, ValidationNel}

    val width = toInt(params("width"), "Width")
    val height = toInt(params("height"), "Height")

    val verticalScale = toDouble(params("vertical_scale"), "Vertical Scale")
    val sideLength = toDouble(params("side_length"), "Side Length")
    val thickness = toDouble(params("thickness"), "Thickness")

    val backgroundColour = parseColour(params("background_colour"), "Background Colour")
    val gridColour = parseColour(params("grid_colour"), "Grid Colour")

    val gridValidation = (width |@| height |@| verticalScale |@| sideLength |@| thickness |@| backgroundColour |@| gridColour) {
      (w, h, scaleV, sl, th, backgroundColour, gridColour) =>

      val mask = Grid.scaledHexMask(sl, thickness = th / 2.0, scaleV = scaleV) // Grid.hexMask(sl, th / 2.0)
      val image = ColouredImage.colourize(mask, on = gridColour, off = backgroundColour)

      Rasterize.superSamplePng(w, h, 4, response.getOutputStream, image)

      ()
    }

    gridValidation match {
      case Success(()) =>
        contentType="image/png"
        ()
      case Failure(errors) =>
        println(s"Errors -> $errors")
        errors.toString()
    }
  }

  def toInt(str:String, name:String) : ValidationNel[String, Int] = {
    import scalaz._
    import Scalaz._
    try { str.toInt.successNel } catch { case ex: NumberFormatException => s"Couldnt parse $name param to int".failureNel }
  }

  def toDouble(str:String, name:String) : ValidationNel[String, Double] = {
    import scalaz._
    import Scalaz._
    try { str.toDouble.successNel } catch { case ex: NumberFormatException => s"Couldnt parse $name param to double".failureNel }
  }

  def parseColour(str:String, name:String) : ValidationNel[String, ColourI] = {
    import scalaz._
    import Scalaz._
    MyParse.colourParser.parse(str) match {
      case Parsed.Success(colour, _) =>
        colour.successNel
      case f@Parsed.Failure(_, _, _) =>
        s"failure parsing $f for param $name".failureNel
    }
  }
}

object MyParse {
  val digits = "0123456789"
  val DecNum = P( CharsWhile(digits.contains(_)).!.map(_.toInt))

  val PostFloat = P("." ~ DecNum.map(_.toDouble))
  val TwoPartFloat = P(DecNum ~ "." ~ DecNum).map { case (a, b) =>
      a.toDouble + s".$b".toDouble
  }

  val Float = P(TwoPartFloat | PostFloat | DecNum.map(_.toDouble))

  val paddedComma = P(" ".rep ~ "," ~ " ".rep)

  val rgbParser = P( "rgb(" ~ DecNum ~ paddedComma ~ DecNum ~ paddedComma ~ DecNum ~ ")").map { case (r, g, b) =>
    Colour.colourI(r, g, b, 255)
  }
  val rgbaParser = P ( "rgba(" ~ DecNum ~ paddedComma ~ DecNum ~ paddedComma ~ DecNum ~ paddedComma ~ Float ~ ")").map { case (r, g, b, a) =>
    Colour.colourI(r, g, b, (a * 255).toInt)
  }
  val colourParser = P (rgbParser | rgbaParser)
}

