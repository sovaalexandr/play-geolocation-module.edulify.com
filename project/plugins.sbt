// Comment to get more information during initialization
logLevel := Level.Warn

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % System.getProperty("play.version", "2.6.7"))

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
