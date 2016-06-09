name := "grid me"

version := "1.0"

organization := "Michael Shaw"

compileOrder := CompileOrder.JavaThenScala

cancelable in Global := true

scalaVersion := "2.11.8"

fork := true

retrieveManaged := true

libraryDependencies += "org.scalatra" % "scalatra_2.11" % "2.4.1"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"

libraryDependencies += "org.scalatra" % "scalatra-scalate_2.11" % "2.4.1"

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.3.9.v20160517"
