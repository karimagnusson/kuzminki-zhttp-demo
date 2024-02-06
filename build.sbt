scalaVersion := "2.13.12"

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
      "com.typesafe" % "config" % "1.4.1",
      "dev.zio" %% "zio" % "2.0.12",
      "dev.zio" %% "zio-http" % "3.0.0-RC1",
      "io.github.karimagnusson" %% "kuzminki-zio-2" % "0.9.5-RC2",
    )
  )

