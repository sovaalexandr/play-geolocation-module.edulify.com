name := "geolocation-java-sample"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayMinimalJava)

scalaVersion := "2.12.4"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  ehcache,
  "com.edulify" %% "geolocation" % "2.2.0"
)

resolvers ++= Seq(
  Resolver.url("Edulify Repository", url("https://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)
)
