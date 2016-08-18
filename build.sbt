
name := "streapler-root"
organization := "rsp"
//version := "0.0.1"
scalaVersion := "2.11.6"

//enablePlugins(JavaAppPackaging)
  
lazy val rspql = (project in file("rspql"))

lazy val streapler = (project in file("streapler")).dependsOn(rspql)

lazy val root = (project in file(".")).
  aggregate(rspql,streapler)
//  settings(
//    aggregate in update := false
//  )
  
  
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
