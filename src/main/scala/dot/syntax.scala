package dot

import cats.implicits._
import cats.{Functor, Monad}

object syntax {
  implicit class StringPathOps(s: String) {
    def in(xs: String*): Boolean = xs.toList.contains(s)
  }

  def not[F[_] : Functor](test: F[Boolean]): F[Boolean] = test.map(!_)

  def when[F[_] : Monad](shouldDo: F[Boolean])(action: F[Unit]): F[Unit] =
    shouldDo.ifM(action, Monad[F].unit)
}
