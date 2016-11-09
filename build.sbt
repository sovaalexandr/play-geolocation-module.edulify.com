name := "geolocation"

version := "2.1.1-SNAPSHOT"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

libraryDependencies ++= Seq(
  javaCore,
  javaWs,
  cache,
  "com.maxmind.geoip2" % "geoip2" % "2.6.0",
  "org.mockito" % "mockito-core" % "2.2.11" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test,
  "com.jayway.awaitility" % "awaitility" % "1.7.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.8" % Test
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q")

organization := "com.edulify"

organizationName := "Edulify.com"

organizationHomepage := Some(new URL("https://edulify.com"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo := {
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some(Resolver.sonatypeRepo("snapshots"))
  else
    Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

startYear := Some(2013)

description := "This is a geolocation module for Playframework."

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("http://edulify.github.io/play-geolocation-module.edulify.com/"))

pomExtra :=
  <scm>
    <url>https://github.com/edulify/play-geolocation-module.edulify.com</url>
    <connection>scm:git:git@github.com:edulify/play-geolocation-module.edulify.com.git</connection>
    <developerConnection>scm:git:https://github.com/edulify/play-geolocation-module.edulify.com.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>megazord</id>
        <name>Megazord</name>
        <email>contact [at] edulify.com</email>
        <url>https://github.com/megazord</url>
      </developer>
      <developer>
        <id>ranierivalenca</id>
        <name>Ranieri Valença</name>
        <email>ranierivalenca [at] edulify.com</email>
        <url>https://github.com/ranierivalenca</url>
      </developer>
    </developers>

scalacOptions := Seq("-feature", "-deprecation")
