ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "org.phylo"

val runHermitGuards: Boolean =
  sys.props.get("runHermit").exists(_.equalsIgnoreCase("true"))

lazy val root = (project in file("."))
  .settings(
    name := "scala-owl-abducer",
    libraryDependencies ++= Seq(
      "net.sourceforge.owlapi" % "owlapi-distribution" % "5.5.0",
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "io.github.liveontologies" % "elk-owlapi" % "0.6.0" % Test,
      "net.sourceforge.owlapi" % "org.semanticweb.hermit" % "1.4.5.519" % Test
    ),
    Test / testOptions ++= {
      if (runHermitGuards) Seq.empty
      else Seq(Tests.Argument(TestFrameworks.ScalaTest, "-l", "org.phylo.abducer.tags.HermitGuard"))
    },
    Test / parallelExecution := true
  )
