package dot

import cats.effect.Sync
import cats.implicits._

object exec {

  case class Proc(exitCode: Int, out: String)

  trait Shell[F[_]] {
    def slurp(cmd: String): Slurp[F]

    def interactive(cmd: String): Interactive[F]
  }

  trait Exec[F[_]] {
    def slurp(cmd: String, args: String*): Slurp[F]

    def interactive(cmd: String, args: String*): Interactive[F]
  }

  case class Slurp[F[_] : Sync](cmd: String, it: F[Proc]) {

    private def successful: F[Proc] = it.ensureOr(proc => new RuntimeException(s"Failed command $cmd\nout:${proc.out}"))(_.exitCode == 0)

    def out: F[String] = successful.map(_.out)

    def succeeded: F[Boolean] = it.map(_.exitCode == 0)

    def failed: F[Boolean] = it.map(_.exitCode != 0)

    def code: F[Int] = it.map(_.exitCode)

    def attempt: F[Unit] = successful.void

  }

  case class Interactive[F[_] : Sync](cmd: String, it: F[Int]) {

    private def successful: F[Int] = it.ensureOr(code => new RuntimeException(s"Failed command $cmd, exit code: $code"))(_ == 0)

    def succeeded: F[Boolean] = it.map(_ == 0)

    def failed: F[Boolean] = it.map(_ != 0)

    def code: F[Int] = it

    def attempt: F[Unit] = successful.void

  }






}
