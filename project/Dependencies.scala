import sbt._

object Dependencies {
  private val simexVersion = "0.9.6"
  private val scalaTestVersion = "3.2.12"

  lazy val all = Seq(
    "io.github.thediscprog" %% "simex-messaging" % simexVersion
  )
}