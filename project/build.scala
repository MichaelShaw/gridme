import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.earldouglas.xwp.JettyPlugin
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object MyScalatraWebAppBuild extends Build {
  val Organization = "Michael Shaw"
  val Name = "gridme-webapp"
  val Version = "0.1.0"
  val ScalaVersion = "2.11.8"
  val ScalatraVersion = "2.4.1"

  lazy val project = Project (
    "my-scalatra-web-app",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      fork := true,
      cancelable in Global := true,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
//      javaOptions ++= Seq("-agentpath:/Applications/YourKit_Java_Profiler_2013_build_13088.app/bin/mac/libyjpagent.jnilib"),
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.eclipse.jetty" % "jetty-webapp" % "9.3.9.v20160517" % "compile;container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "ar.com.hjg" % "pngj" % "2.1.0",
        "com.lihaoyi" %% "fastparse" % "0.3.7"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
//            new RichFile(new File("src")) / "web" / "webapp" / "WEB-INF" / "templates",
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  ).enablePlugins(JettyPlugin)
}
