import sbt._
import sbt.Keys._
import scala.Some

object IiCoreBuild extends Build {

  val buildSettings = Seq (
    organization := "com.owlunit",
    version := "0.4-SNAPSHOT",
    scalaVersion := "2.9.1"
  )

  lazy val root = Project(
    id = "core",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        Dependency.neo4j,
        Dependency.neo4jREST,
        Dependency.akka,
        Dependency.akkaTest,
        Dependency.slf4s,
        Dependency.logback,
        Dependency.specs2
        )
      )
    )


  /////////////////////
  // Settings
  /////////////////////

  override lazy val settings = super.settings ++ buildSettings ++ Seq(
    resolvers ++= Seq(typesafe, maven),
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked"),
    publishTo := Some(Resolver.file("file",  localRepoLocation))
  )

  val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val maven = "Neo4j Maven 2 release repository" at "http://m2.neo4j.org/releases"

  val localRepoLocation = new File( "../../owlunit.github.com/repo/ivy/" )

  /////////////////////
  // Dependencies
  /////////////////////

  object Dependency {

    val neo4j       = "org.neo4j"                 %  "neo4j"                % "1.8"
    val neo4jREST   = "org.neo4j"                 %  "neo4j-rest-graphdb"   % "1.8"

    val akka        = "com.typesafe.akka"         % "akka-actor"            % "2.0.4"
    val akkaTest    = "com.typesafe.akka"         % "akka-testkit"          % "2.0.4"

    val slf4s       = "com.weiglewilczek.slf4s"   %% "slf4s"                % "1.0.7"
    val logback     = "ch.qos.logback"            %  "logback-classic"      % "1.0.6"  % "test"
    val specs2      = "org.specs2"                %% "specs2"               % "1.9"    % "test"

  }

}
