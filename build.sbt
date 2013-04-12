name := "odds"

organization := "EPFL"

version := "0.1-SNAPSHOT"

scalaOrganization := "org.scala-lang.virtualized"

scalaVersion := Option(System.getenv("SCALA_VIRTUALIZED_VERSION")).getOrElse("2.10.0")

//--- Dependencies

resolvers ++= Seq(
    ScalaToolsSnapshots,
    "Sonatype Public" at "https://oss.sonatype.org/content/groups/public")

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10.0-RC5" % "2.0.M5-B1" % "test",
    "EPFL" %% "lms" % "0.3-SNAPSHOT")

//--- End of Dependencies

// General compiler options
scalacOptions ++= Seq(
  "-deprecation", "-unchecked", "-Xexperimental", "-P:continuations:enable",
  "-Yvirtualize", "-feature", "-language:higherKinds")

// Documentation (scaladoc) options
scalacOptions in doc += "-external-urls:scala=http://www.scala-lang.org/"

// Our tests are not threadsafe so disabling parallel execution for now
parallelExecution in Test := false

// disable publishing of main docs
publishArtifact in (Compile, packageDoc) := false

// continuations
autoCompilerPlugins := true

libraryDependencies <<= (scalaVersion, libraryDependencies) { (ver, deps) =>
    deps :+ compilerPlugin("org.scala-lang.plugins" % "continuations" % ver)
}