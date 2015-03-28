import play.PlayScala

name := """Thera Zeta"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "Typesafe repository snapshots"    at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases"     at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  "Websudos releases"                at "http://maven.websudos.co.uk/ext-release-local"
)

libraryDependencies ++= Seq( ws, jdbc, anorm, cache,
  "joda-time" % "joda-time" % "2.7",  //Used in Phantom DSL
  "com.websudos" % "phantom-dsl_2.11" % "1.5.0",
  "com.websudos" % "phantom-zookeeper_2.11" % "1.5.0",
  "com.websudos"  %% "phantom-testing" % "1.5.0",
  "jp.t2v" %% "play2-auth" % "0.13.0",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.cassandraunit" %  "cassandra-unit" % "2.0.2.5" //Overwriting Phantom's broken dependency
    excludeAll(
      ExclusionRule("org.slf4j", "slf4j-log4j12"),
      ExclusionRule("org.slf4j", "slf4j-jdk14")))

fork in Test:= false //We use this to debug Unit Tests on Intellij

