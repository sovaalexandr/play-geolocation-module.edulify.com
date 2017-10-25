name := "geolocation"

version := "2.2.0"

scalaVersion := "2.12.4"

lazy val root = (project in file(".")).enablePlugins(PlayMinimalJava)

libraryDependencies ++= Seq(
  javaWs,
  cacheApi,
  guice,
  ehcache % Test, // Should be removed with deprecated GeolocationService and GeolocationCache
  "org.mockito" % "mockito-core" % "2.11.0" % Test,
  "com.jayway.awaitility" % "awaitility" % "1.7.0" % Test
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
