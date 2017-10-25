// Comment to get more information during initialization
logLevel := Level.Warn

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("play.version", "2.6.6"))

// Provides PGP signing for Sonatype repo
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")
