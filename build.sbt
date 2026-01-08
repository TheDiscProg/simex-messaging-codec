import sbt.librarymanagement.CrossVersion
import sbt.url
import xerial.sbt.Sonatype._


lazy val scala3 = "3.7.3"
lazy val supportedScalaVersions = List(scala3)

lazy val commonSettings = Seq(
  scalaVersion := scala3,
  libraryDependencies ++= Dependencies.all
)

lazy val root = (project in file("."))
  .enablePlugins(
    ScalafmtPlugin
  )
  .settings(
    commonSettings,
    name := "simex-messaging-codec",
    scalacOptions ++= Scalac.options,
    crossScalaVersions := supportedScalaVersions
  )

ThisBuild / version := "0.1.0"
ThisBuild / organization := "io.github.thediscprog"
ThisBuild / organizationName := "thediscprog"
ThisBuild / organizationHomepage := Some(url("https://github.com/TheDiscProg"))

ThisBuild / description := "Simple Message Exchange (SIMEX) CODEC"

// Sonatype/Maven Publishing
ThisBuild / publishMavenStyle := true
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeProfileName := "io.github.thediscprog"
ThisBuild / licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage := Some(url("https://github.com/TheDiscProg/simex-messaging-codec"))
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("TheDiscProg", "simex-messaging-codec", "TheDiscProg@gmail.com"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/TheDiscProg/simex-messaging-code"),
    "scm:git@github.com:thediscprog/simex-messaging-codec.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "thediscprog",
    name = "TheDiscProg",
    email = "TheDiscProg@gmail.com",
    url = url("https://github.com/TheDiscProg")
  )
)

usePgpKeyHex("FC6901A47E5DA2533DCF25D51615DCC33B57B2BF")

sonatypeCredentialHost := "central.sonatype.com"
sonatypeRepository := "https://central.sonatype.com/api/v1/publisher/"

ThisBuild / versionScheme := Some("early-semver")

addCommandAlias("cleanTest", ";clean;scalafmt;test:scalafmt;test;")
addCommandAlias("cleanCoverage", ";clean;scalafmt;test:scalafmt;coverage;test;coverageReport;")
