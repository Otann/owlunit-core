import sbt._
import sbt.Keys._

object IiCoreBuild extends Build {

  val buildSettings = Seq (
    organization := "com.owlunit",
    version := "0.4.1-SNAPSHOT",
    scalaVersion := "2.10.0"
  )

  lazy val root = Project(
    id = "core",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
        libraryDependencies ++= dependencies,
        resolvers ++= customResolvers
      )
    )


  /////////////////////
  // Settings
  /////////////////////

  override lazy val settings = super.settings ++ buildSettings ++ Seq(
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked")
  )

  val customResolvers = Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Neo4j Maven 2 release repository" at "http://m2.neo4j.org/releases"
  )

  /////////////////////
  // Dependencies
  /////////////////////

  object V {
    val neo4j = "1.9"
    val akka = "2.1.4"
    val titan = "0.3.1"
    val tinkerpop = "2.3.0"
  }

  val dependencies = Seq(

    // Workers implemented in akka
    "com.typesafe.akka"        %% "akka-actor"           % V.akka,
    "com.typesafe.akka"        %% "akka-testkit"         % V.akka,

    // Graph interfaces
    "com.tinkerpop.blueprints" %  "blueprints-core"      % V.tinkerpop,
    "com.tinkerpop.gremlin"    %  "gremlin-java"         % V.tinkerpop,
    "com.michaelpollmeier"     %  "gremlin-scala"        % V.tinkerpop,

    // Graph backend implementation
    "com.thinkaurelius.titan"  %  "titan-berkeleyje"     % V.titan exclude("org.slf4j", "slf4j-log4j12"),
    "com.thinkaurelius.titan"  %  "titan-core"           % V.titan exclude("org.slf4j", "slf4j-log4j12"),

    // Graph backend implementation
    "org.neo4j"                %  "neo4j"                % V.neo4j,
    "org.neo4j"                %  "neo4j-rest-graphdb"   % V.neo4j,

    // Logging
    "com.typesafe"             %% "scalalogging-slf4j"   % "1.0.1",
    "ch.qos.logback"           %  "logback-classic"      % "1.0.6"  % "test",
    "com.typesafe.akka"        %% "akka-slf4j"           % V.akka   % "test",
    "org.specs2"               %% "specs2"               % "1.13"   % "test",
    "org.scalatest"            %% "scalatest"            % "1.9.1"  % "test"

  )

}
