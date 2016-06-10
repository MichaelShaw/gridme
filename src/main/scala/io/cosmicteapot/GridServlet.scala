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
import scala.util.Try
import org.scalatra.scalate.ScalateSupport

import io.cosmicteapot.Colour.ColourI

object JettyLauncher extends App { // this is my entry object as specified in sbt project definition
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
  get("/") {
    contentType="text/html"
    jade("/index", "layout" -> "WEB-INF/templates/layouts/default.jade")
  }


  get("/grid") {
    val pngStream = for {
      widthInTiles <- toInt(params("width_tiles"))
      heightInTiles <- toInt(params("height_tiles"))
//      _ = print("I HAVE WIDTH AND HEIGHT")
      tileHeight <- toInt(params("tile_height"))
      tileWidth <- toInt(params("tile_width"))
      backgroundColour <- parseColour(params("background_colour"))
      gridColour <- parseColour(params("grid_colour"))
    } yield {
      val intImage = Grid.generate(widthInTiles, heightInTiles, tileWidth, tileHeight, backgroundColour, gridColour)
      val outputStream = new ByteArrayOutputStream()
      IntImage.writeToOutputStream(intImage, outputStream)
      outputStream
    }

    pngStream match {
      case Some(stream) =>
        contentType="image/png"
        stream.toByteArray
      case None => <h2>Problem with params :-(</h2>
    }
  }

  def toInt(str:String) : Option[Int] = Try(str.toInt).toOption
  def parseColour(str:String) : Option[ColourI] = {
    MyParse.colourParser.parse(str) match {
      case Parsed.Success(colour, _) =>
        Some(colour)
      case f@Parsed.Failure(_, _, _) =>
        println(s"failure parsing $f")
        None
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

