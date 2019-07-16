name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.1.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.spray" %% "spray-json" % "1.3.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
