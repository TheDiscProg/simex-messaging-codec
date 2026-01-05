import sbt._

object Dependencies {
  private val slogicVersion = "0.3.1"
  private val simexVersion = "0.9.5"
  private val scalaTestVersion = "3.2.19"

  lazy val all = Seq(
    "io.github.thediscprog" %% "slogic" % slogicVersion,
    "io.github.thediscprog" %% "simex-messaging" % simexVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )
}