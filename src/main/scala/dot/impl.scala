package dot

import java.util.Random

import cats.effect.{Sync}
import cats.implicits._
import dot.exec.{Exec, Interactive, Proc, Shell, Slurp}
import dot.syntax.{not, when}
import org.zeroturnaround.exec.ProcessExecutor

import scala.collection.JavaConverters._

object impl {

  class ExecImpl[F[_] : Sync]() extends Exec[F] {

    override def slurp(cmd: String, args: String*): Slurp[F] =
      Slurp(
        s"$cmd ${args.toList.mkString(" ")}",
        Sync[F].delay {
          val p = new ProcessExecutor()
            .command((cmd :: args.toList).asJava)
            .readOutput(true)
            .redirectError(System.err)
            .execute()

          Proc(p.getExitValue, p.getOutput.getString)
        }
      )

    override def interactive(cmd: String, args: String*): Interactive[F] = {
      Interactive(
        s"$cmd ${args.toList.mkString(" ")}",
        Sync[F].delay {
          new ProcessExecutor()
            .command((cmd :: args.toList).asJava)
            .redirectOutput(System.out)
            .redirectError(System.err)
            .redirectInput(System.in)
            .execute()
            .getExitValue
        }
      )
    }
  }

  class OutImpl[F[_] : Sync]() extends Out[F] {
    override def println(str: String): F[Unit] = Sync[F].delay {
      scala.Predef.println(str)
    }.void
  }

  class ShellImpl[F[_] : Sync](exec: Exec[F]) extends Shell[F] {
    private def toShell(cmd: String): (String, Seq[String]) =
      ("sh", Seq("-c", cmd))

    override def slurp(cmd: String): Slurp[F] = {
      val (c, args) = toShell(cmd)
      exec.slurp(c, args: _*)
    }

    override def interactive(cmd: String): Interactive[F] = {
      val (c, args) = toShell(cmd)
      exec.interactive(c, args: _*)
    }
  }

  class PkgImpl[F[_] : Sync](exec: Exec[F], out: Out[F]) extends Pkg[F] {

    private def installed(pkg: String): F[Boolean] =
      exec.slurp("pacman", "-Q", pkg).succeeded

    private def ifNotInstalled(pkg: String)(action: F[Unit]): F[Unit] =
      when(not(installed(pkg)))(out.println(s"Installing $pkg") *> action)

    override def aur(pkg: String): F[Unit] =
      ifNotInstalled(pkg)(exec.interactive("yay", "-S", pkg, "--needed").attempt)

    override def pac(pkg: String): F[Unit] =
      ifNotInstalled(pkg)(exec.interactive("sudo", "pacman", pkg, "-S", pkg, "--needed").attempt)

  }

  class FilesImpl[F[_] : Sync](ex: Exec[F], sh: Shell[F]) extends Files[F] {

    import java.io.File
    import java.nio.file.{Paths, Files => JFiles}

    override def makeExecutable(path: AbsPath) : F[Unit] =
      when(ex.interactive("test", "-x", path.raw).failed) {
        ex.interactive("chmod", "+x", path.raw).attempt
      }

    override def exists(path: AbsPath): F[Boolean] = Sync[F].delay(new File(path.raw).exists())

    private def filesEqual(a: AbsPath, b: AbsPath): F[Boolean] =
      ex.slurp("diff", a.raw, b.raw).succeeded

    override def merge(from: AbsPath, to: AbsPath): F[Unit] =
      when(not(filesEqual(from, to))) {
        ex.slurp("mkdir", "-p", to.dir.raw).attempt *>
          when(not(exists(to)))(ex.slurp("touch", to.raw).attempt) *>
          ex.interactive("meld", from.raw, to.raw).attempt
      }

    override def mergeRoot(from: AbsPath, to: AbsPath): F[Unit] =
      when(not(filesEqual(from, to))) {
        ex.interactive("sudo", "mkdir", "-p", to.dir.raw).attempt *>
          when(not(exists(to)))(ex.interactive("sudo", "touch", to.raw).attempt) *>
          ex.interactive("sudo", "meld", from.raw, to.raw).it.void
      }

    def safeEditFileLinesUtf8(f: AbsPath, sudoWrite: Boolean)(edit: List[String] => Either[String, List[String]]): F[Unit] =
      for {
        lines <- Sync[F].delay(JFiles.readAllLines(Paths.get(f.raw)).asScala.toList)
        edited <- Sync[F].fromEither(edit(lines).leftMap(new RuntimeException(_)))
        _ <-
          if (lines == edited)
            Sync[F].unit
          else
            for {
              rng <- Sync[F].delay(new Random().nextLong())
              temp = Paths.get("/tmp", s"dot-edit-${rng.toString}")
              _ <- Sync[F].delay {
                JFiles.write(temp, edited.asJava)
                ()
              }
              _ <- sh.interactive("$EDITOR " + temp.toString).attempt
              sudo = if (sudoWrite) " sudo" else ""
              _ <- sh.interactive(s"cat ${temp.toString} |$sudo tee ${f.raw} &>2").attempt
            } yield ()
      } yield ()

    override def replaceLine(file: AbsPath, existing: String, desired: String, sudoWrite: Boolean): F[Unit] =
      safeEditFileLinesUtf8(file, sudoWrite) { lines =>
        if (lines.contains(desired))
          Either.right(lines)
        else
          lines.count(_ == existing) match {
            case 1 => Either.right(lines.map(l => if (l == existing) desired else l))
            case 0 => Either.left(s"Can't find a line to replace -- $existing")
            case n => Either.left(s"Found too many ($n) lines to replace -- $existing")
          }
      }

    override def ensureLine(file: AbsPath, desired: String, sudoWrite: Boolean): F[Unit] =
      safeEditFileLinesUtf8(file, sudoWrite) { lines =>
        Either.right(if (lines.contains(desired)) lines else lines :+ desired)
      }

    override def list(path: AbsPath): F[List[AbsPath]] =
      Sync[F].delay(new File(path.raw).list().toList.map(path / _))
  }

}
