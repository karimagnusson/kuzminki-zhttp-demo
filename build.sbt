scalaVersion := "2.13.8"

name := "kuzminki-zhttp-demo"

version := "0.9.0"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

lazy val root = (project in file("."))
  .settings(
    name := "kuzminki-zio",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % "2.13.8",
      "dev.zio" %% "zio" % "1.0.12",
      "io.d11" %% "zhttp" % "1.0.0.0-RC29",
      "org.postgresql" % "postgresql" % "42.2.24",
      "com.typesafe.play" %% "play-json" % "2.9.2"
    )
  )

