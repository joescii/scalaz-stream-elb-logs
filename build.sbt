name := "scalaz-stream-elb-logs"
organization := "com.joescii"
homepage := Some(url("https://github.com/joescii/scalaz-stream-elb-logs"))
version := "0.0.1"
scalaVersion := "2.11.8"

lazy val http4sVersion = "0.14.1a"

libraryDependencies ++= Seq(
  "org.http4s"                  %% "http4s-dsl"           % http4sVersion,
  "org.http4s"                  %% "http4s-blaze-client"  % http4sVersion,
  "org.scalatest"               %% "scalatest"            % "2.2.6" % "test,it",
  "org.scalacheck"              %% "scalacheck"           % "1.13.2" % "test"
)

lazy val p = project.in(file(".")).
  configs(IntegrationTest).
  settings(Defaults.itSettings: _*)
