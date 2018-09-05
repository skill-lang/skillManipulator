name := "skillManipulator"

version := "0.9"

scalaVersion := "2.12.4"

javacOptions ++= Seq("-encoding", "UTF-8")

compileOrder := CompileOrder.Mixed

libraryDependencies ++= Seq(
    "org.junit.jupiter" % "junit-jupiter-engine" % "5.0.0-M3" % "test"
)

lazy val root = (project in file(".")).
    settings(
            assemblyJarName in assembly := "skillManipulator.jar",
            test in assembly := {},
            exportJars := true,
            
            mainClass := Some("de.ust.skill.manipulator.CLI"),
            
            libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
    )

