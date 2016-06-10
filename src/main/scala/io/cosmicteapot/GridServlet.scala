package io.cosmicteapot

import java.io.ByteArrayOutputStream

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.scalatra._
import org.scalatra.LifeCycle
import javax.servlet.ServletContext

import scala.util.Try
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

class GridServlet extends ScalatraServlet {
  get("/") {
    <html>
      <title>gridme</title>
      <body>
        <h1>Hello, world!</h1>
        <form method="get" action="/grid">
          Width (in tiles) <input type="text" name="width_tiles" value="10" /> <br />
          Height (in tiles) <input type="text" name="height_tiles" value="10" /> <br/>
          Tile Width (px) <input type="text" name="tile_height" value="32" /> <br/>
          Tile Height (px) <input type="text" name="tile_width" value="32" /> <br/>
          Background Colour (R,G,B,A) <input type="text" name="background_colour" value="255,255,255,0" /> <br/>
          Grid Colour (R,G,B,A) <input type="text" name="grid_colour" value="60,60,60,255" /> <br/>
          <input type="submit" value="Create Grid" />
        </form>

      </body>
    </html>
  }

  get("/grid") {
    val pngStream = for {
      widthInTiles <- toInt(params("width_tiles"))
      heightInTiles <- toInt(params("height_tiles"))
      _ = print("I HAVE WIDTH AND HEIGHT")
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
    val parts = str.split(",")

    val ints = for {
      part <- parts
      n <- toInt(part.trim())
    } yield Colour.clamp(n, 0, 255)

    if (ints.length == 4) {
      Some(Colour.colourI(ints))
    } else {
      None
    }
  }
}

