
name := "rsp"
organization := "r2rml-js"
version := "0.0.1"
scalaVersion := "2.11.8"

  
libraryDependencies ++= Seq(
  
)

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "gsn" at "http://osper.epfl.ch:8081/artifactory/gsn-release",
  "plord" at "http://homepages.cs.ncl.ac.uk/phillip.lord/maven",
  Resolver.sonatypeRepo("public")
)

enablePlugins(ScalaJSPlugin)

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

scalacOptions ++= Seq("-feature","-deprecation")