import sbt._

name         := "java"

version      := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaEbean //,
//    "com.edulify" % "geolocation_2.10" % "1.1.3"
)