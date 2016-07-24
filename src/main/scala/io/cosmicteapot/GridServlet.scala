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

object Validation {
  implicit class Valid[T](v:Either[String, T]) {
    def |@|[B](other:Either[String, B]) : Either[List[String], (T,B)] = {
      v match {
        case Left(error) =>
          other match {
            case Left(oError) => Left(List(error, oError))
            case Right(_) => Left(List(error))
          }
        case Right(va) =>
          other match {
            case Left(oError) => Left(List(oError))
            case Right(vo) => Right((va, vo))
          }
      }
    }
  }

  implicit class ValidList[T](v:Either[List[String], T]) {
    def |@|[B](other:Either[String, B]) : Either[List[String], (T,B)] = {
      v match {
        case Left(errors) =>
          other match {
            case Left(oError) => Left(errors ++ List(oError))
            case Right(_) => Left(errors)
          }
        case Right(va) =>
          other match {
            case Left(oError) => Left(List(oError))
            case Right(vo) => Right((va, vo))
          }
      }
    }
  }

  def apply6[A,B,C,D,E,F, O](v:Either[List[String],(((((A, B),C),D),E),F)]) (mf:(A, B, C, D, E, F) => O) : Either[List[String], O] = {
    v match {
      case l @ Left(errors) => Left(errors)
      case r @ Right((((((a, b),c),d),e),f)) => Right(mf(a,b,c,d,e,f))
    }
  }

  def apply7[A,B,C,D,E,F,G, O](v:Either[List[String],((((((A, B),C),D),E),F),G)]) (mf:(A, B, C, D, E, F, G) => O) : Either[List[String], O] = {
    v match {
      case l @ Left(errors) => Left(errors)
      case r @ Right(((((((a, b),c),d),e),f),g)) => Right(mf(a,b,c,d,e,f,g))
    }
  }
}

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
    layoutTemplate("/WEB-INF/templates/views/index.jade")
  }

  get("/squares/new") {
    contentType="text/html"
    layoutTemplate("/WEB-INF/templates/views/squares.jade")
  }

  get("/squares.png") {
    import Validation._
    val widthInTiles = toInt(params("width_tiles"), "Width Tiles")
    val heightInTiles = toInt(params("height_tiles"), "Height Tiles")
    val tileWidth = toInt(params("tile_width"), "Tile Width")
    val tileHeight = toInt(params("tile_height"), "Tile Height")

    val backgroundColour = parseColour(params("background_colour"), "Background Colour")
    val gridColour = parseColour(params("grid_colour"), "Grid Colour")

    val gridValidation = apply6(widthInTiles |@| heightInTiles |@| tileWidth |@| tileHeight |@| backgroundColour |@| gridColour) {
      (w, h, tw, th, backgroundColour, gridColour) =>

      val width = w * tw
      val height = h * th

      val mask = Grid.squareMask(tw, th, offset = 0.5, thickness = 0.5)
      val image = ColouredImage.colourize(mask, on = gridColour, off = backgroundColour)

      Rasterize.png(width, height, response.getOutputStream, image)

      ()
    }

    gridValidation match {
      case Right(()) =>
        contentType="image/png"
        ()
      case Left(errors) =>
        println(s"Errors -> $errors")
        errors.toString()
    }
  }

  get("/hexes/new") {
    contentType="text/html"
    layoutTemplate("/WEB-INF/templates/views/hexes.jade")
  }

  get("/hexes.png") {
    import Validation._

    val width = toInt(params("width"), "Width")
    val height = toInt(params("height"), "Height")

    val verticalScale = toDouble(params("vertical_scale"), "Vertical Scale")
    val sideLength = toDouble(params("side_length"), "Side Length")
    val thickness = toDouble(params("thickness"), "Thickness")

    val backgroundColour = parseColour(params("background_colour"), "Background Colour")
    val gridColour = parseColour(params("grid_colour"), "Grid Colour")

    val gridValidation = apply7(width |@| height |@| verticalScale |@| sideLength |@| thickness |@| backgroundColour |@| gridColour) {
      (w, h, scaleV, sl, th, backgroundColour, gridColour) =>

      val mask = Grid.scaledHexMask(sl, thickness = th / 2.0, scaleV = scaleV) // Grid.hexMask(sl, th / 2.0)
      val image = ColouredImage.colourize(mask, on = gridColour, off = backgroundColour)

      Rasterize.superSamplePng(w, h, 4, response.getOutputStream, image)

      ()
    }

    gridValidation match {
      case Right(()) =>
        contentType="image/png"
        ()
      case Left(errors) =>
        println(s"Errors -> $errors")
        errors.toString()
    }
  }

  def toInt(str:String, name:String) : Either[String, Int] = {
    try { Right(str.toInt)} catch { case ex: NumberFormatException => Left(s"Couldnt parse $name param to int") }
  }

  def toDouble(str:String, name:String) : Either[String, Double] = {
    try { Right(str.toDouble)} catch { case ex: NumberFormatException => Left(s"Couldnt parse $name param to double") }
  }

  def parseColour(str:String, name:String) : Either[String, ColourI] = {
    MyParse.colourParser.parse(str) match {
      case Parsed.Success(colour, _) =>
        Right(colour)
      case f@Parsed.Failure(_, _, _) =>
        Left(s"failure parsing $f for param $name")
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

