name := "skillManipulator"

version := "0.9"

scalaVersion := "2.12.4"

javacOptions ++= Seq("-encoding", "UTF-8")

compileOrder := CompileOrder.Mixed

libraryDependencies ++= Seq(
    "org.junit.platform" % "junit-platform-runner" % "1.3.1" % "test",
    "org.junit.jupiter" % "junit-jupiter-engine" % "5.3.1" % "test",
    "org.junit.vintage" % "junit-vintage-engine" % "5.3.1" % "test"
)

javaOptions ++= Seq("-Xmx4G","-Xms4G","-XX:MaxHeapFreeRatio=100")

resolvers in ThisBuild += Resolver.jcenterRepo

lazy val root = (project in file(".")).
    settings(
            assemblyJarName in assembly := "skillManipulator.jar",
            test in assembly := {},
            exportJars := true,
            
            mainClass := Some("de.ust.skill.manipulator.CLI"),
            
            libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
    )

