
name := "streapler"
organization := "rsp"
version := "0.0.1"
scalaVersion := "2.11.6"

enablePlugins(JavaAppPackaging)
  
libraryDependencies ++= Seq(
  "org.apache.jena" % "apache-jena-libs" % "2.13.0",
  "org.antlr" % "antlr" % "3.2",
  "org.semanticweb.hermit" % "HermiT" % "1.3.7.3" intransitive,
  "net.sourceforge.owlapi" % "owlapi-api" % "3.4.4" intransitive,
  "net.sourceforge.owlapi" % "owlapi-apibinding" % "3.4.4",
  "org.apache.spark" % "spark-streaming_2.11" % "1.4.1",
  "org.apache.spark" % "spark-mllib_2.11" % "1.4.1",
  "com.typesafe.play" %% "play-ws" % "2.3.8",  
  "eu.trowl" % "trowl-core" % "1.4",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "junit" % "junit" % "4.12" % "test"
)

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "gsn" at "http://osper.epfl.ch:8081/artifactory/gsn-release",
  "plord" at "http://homepages.cs.ncl.ac.uk/phillip.lord/maven"
)

mainClass in Compile := Some("rsp.engine.rewriting.Experiments")

scriptClasspath := Seq("*")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

scalacOptions ++= Seq("-feature","-deprecation")