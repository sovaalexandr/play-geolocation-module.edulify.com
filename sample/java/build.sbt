name := "geolocation-java-sample"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  // Add your project dependencies here,
  javaCore,
  "com.edulify" %% "geolocation" % "2.2.0"
)

resolvers ++= Seq(
  Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)
)
