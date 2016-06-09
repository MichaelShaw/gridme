package io.cosmicteapot

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.scalatra._
import org.scalatra.LifeCycle
import javax.servlet.ServletContext

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
    context mount (new ScalatraExample, "/*")
  }
}

class ScalatraExample extends ScalatraServlet {
  get("/") {
    <body>
      <h1>Hello, world!</h1>
      <a href="/grid">Grid</a>
    </body>
  }

  get("/grid") {
    <h1>This is a Grid</h1>
  }
}