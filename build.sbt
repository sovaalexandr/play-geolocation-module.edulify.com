
val javacSettings = Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint:deprecation",
  "-Xlint:unchecked"
)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.4",
  organization := "com.edulify",
  organizationName := "Edulify.com",
  organizationHomepage := Some(new URL("https://edulify.com")),
  startYear := Some(2013),
  licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("http://edulify.github.io/play-geolocation-module.edulify.com/")),
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
</developers>,
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := { Some(if (isSnapshot.value) Resolver.sonatypeRepo("snapshots") else "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2") },
  scalacOptions := Seq("-feature", "-deprecation"),
  javacOptions in (Compile, doc) ++= javacSettings,
  javacOptions in Test ++= javacSettings,
  javacOptions in IntegrationTest ++= javacSettings,
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a"),
)

val disableDocs = Seq[Setting[_]](
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

val disablePublishing = Seq[Setting[_]](
  publishArtifact := false,
  // The above is enough for Maven repos but it doesn't prevent publishing of ivy.xml files
  publish := {},
  publishLocal := {}
)

val playWsStandalone = "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.3"
val junit = "junit" % "junit" % "4.12" % Test
val junitInterface = "com.novocode" % "junit-interface" % "0.11" % Test
val mockito = "org.mockito" % "mockito-core" % "2.11.0" % Test
val hamcrest = "org.hamcrest" % "hamcrest-core" % "1.3" % Test

lazy val root = (project in file("."))
  .enablePlugins(PlayMinimalJava)
  .aggregate(geolocation, `geolocation-guice`, freegeoip, `freegeoip-guice`, `maxmind-geoip2web`, `maxmind-geoip2web-guice`, `javaSample`)
  .settings(commonSettings, disablePublishing, disableDocs, name := "edulify-geolocation")

lazy val geolocation = (project in file("geolocation"))
  .settings(
    name := "geolocation",
    description := "This is a geolocation module for Playframework.",
    commonSettings
  )
  .settings(libraryDependencies ++= Seq(
    component("play-cache"),
    ehcache % Test, // Should be removed with deprecated GeolocationService and GeolocationCache
    junit,
    junitInterface,
    mockito
  ))

lazy val `geolocation-guice` = (project in file("geolocation-guice"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    guice,
    hamcrest
  ))
  .dependsOn(geolocation % "test->test;compile->compile")

lazy val freegeoip = (project in file("freegeoip"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    playWsStandalone
  ))
  .dependsOn(geolocation)

lazy val `freegeoip-guice` = (project in file("freegeoip-guice"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    component("play-server") % Test,
    javaCore % Test,
    component("play-test") % Test,
    component("play-ahc-ws") % Test,
    component("play-akka-http-server") % Test
  ))
  .dependsOn(freegeoip, `geolocation-guice` % "test->test;compile->compile")

lazy val `maxmind-geoip2web` = (project in file("maxmind-geoip2web"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    playWsStandalone
  ))
  .dependsOn(geolocation)

lazy val `maxmind-geoip2web-guice` = (project in file("maxmind-geoip2web-guice"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    component("play-server") % Test,
    javaCore % Test,
    component("play-ahc-ws") % Test,
    component("play-akka-http-server") % Test
  ))
  .dependsOn(`maxmind-geoip2web`, `geolocation-guice` % "test->test;compile->compile")

lazy val `javaSample` = (project in file("sample/java"))
  .enablePlugins(PlayMinimalJava)
  .settings(routesGenerator := InjectedRoutesGenerator)
  .settings(
    commonSettings,
    disableDocs,
    disablePublishing
  )
  .settings(libraryDependencies ++= Seq(
    ehcache, ws
  ))
  .dependsOn(`freegeoip-guice`)
