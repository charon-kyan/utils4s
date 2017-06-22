import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object KfkUtils4sBuild extends Build {

  lazy val kfkutils4sRoot = (project in file("."))
    .aggregate(kfkutils4sBase)
    .aggregate(kfkutils4sKafka8)
    .aggregate(kfkutils4sKafka9)

  lazy val commonSettings = Seq(
    organization := "me.charon.kfkutils4s",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.11",

    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "org.slf4s" %% "slf4s-api" % "1.7.12",
      "org.slf4j" % "slf4j-log4j12" % "1.7.12",

      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    ),

    excludeDependencies ++= Seq(
      SbtExclusionRule("org.slf4j", "log4j-over-slf4j")
    )
  )

  lazy val kfkutils4sBase = (project in file("base")).
    settings(commonSettings: _*).
    settings(
      name := "kfkutils4s-base",

      retrieveManaged := true,

      libraryDependencies ++= Seq(
      ),

      excludeDependencies ++= Seq(
      )
    )

  lazy val kfkutils4sKafka8 = (project in file("kafka8")).
    settings(commonSettings: _*).
    settings(
      name := "kfkutils-kafka8",

      retrieveManaged := true,

      libraryDependencies ++= Seq(
        "org.apache.kafka" % "kafka_2.11" % "0.8.2.1",
        "org.apache.kafka" % "kafka-clients" % "0.8.2.1"
      ),

      excludeDependencies ++= Seq(
        SbtExclusionRule("ch.qos.logback", "logback-classic")
      )
    ).dependsOn(kfkutils4sBase)

  lazy val kfkutils4sKafka9 = (project in file("kafka9")).
    settings(commonSettings: _*).
    settings(
      name := "kfkutils-kafka9",

      retrieveManaged := true,

      libraryDependencies ++= Seq(
        "org.apache.kafka" % "kafka-clients" % "0.9.0.1"
      ),

      excludeDependencies ++= Seq(
        SbtExclusionRule("ch.qos.logback", "logback-classic")
      )
    ).dependsOn(kfkutils4sBase)
}
