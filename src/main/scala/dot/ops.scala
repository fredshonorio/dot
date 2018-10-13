package dot

import java.nio.file.Path

trait Symbolic {
  def src: AbsPath
  def dst: AbsPath
  def `/`(p: String): Symbolic
}

object Symbolic {

  def mk(_src: AbsPath, _dst: AbsPath): Symbolic =
    new Symbolic {
      override def src: AbsPath = _src
      override def dst: AbsPath = _dst
      override def `/`(p: String): Symbolic = mk(_src / p, _dst / p)
    }

  def HOME: Symbolic = mk(AbsPath.CWD / "HOME", AbsPath.HOME)
  def ROOT: Symbolic = mk(AbsPath.CWD / "ROOT", AbsPath.ROOT)
  def `~`: Symbolic = HOME
}

trait AbsPath {
  def dir: AbsPath
  def raw: String
  def `/`(n: String): AbsPath
  def filename: String = path.getFileName.toString
  def path: Path = java.nio.file.Paths.get(raw)
}

object AbsPath {
  import java.nio.file.Paths
  private def appendUnsafe(dir: String, s: String): AbsPath = {
    // TODO: assert s valid path part
    unsafe(Paths.get(dir, s).toString)
  }

  def unsafe(f: String): AbsPath = {
    val x = {
      val y = Paths.get(f)
      assert(y.isAbsolute, y + " is not absolute")
      y.toAbsolutePath
    }

    val _raw = x.toString
    val parent = if (x.getRoot.equals(x)) x else x.getParent

    new AbsPath {
      override def dir: AbsPath = unsafe(parent.toString)
      override def raw: String = _raw
      override def `/`(s: String): AbsPath = appendUnsafe(_raw, s)
    }
  }

  val HOME: AbsPath = unsafe(System.getProperty("user.home"))
  val `~`: AbsPath =  HOME

  lazy val CWD: AbsPath = unsafe(System.getProperty("user.dir"))

  val ROOT: AbsPath = new AbsPath {
    override def dir: AbsPath = ROOT
    override def raw: String = "/"
    override def `/`(n: String): AbsPath = appendUnsafe("/", n)
  }
}


trait Pkg[F[_]] {
  def aur(name: String): F[Unit]
  def pac(name: String): F[Unit]
}

trait Files[F[_]] {
  def makeExecutable(path: AbsPath): F[Unit]
  def list(path: AbsPath): F[List[AbsPath]]
  def exists(path: AbsPath): F[Boolean]
  def merge(from: AbsPath, to: AbsPath): F[Unit]
  def merge_(from: AbsPath, to: AbsPath): F[Unit] // not needed
  def replaceLine(file: AbsPath, existing: String, desired: String, sudoWrite: Boolean = false): F[Unit]
  def ensureLine(file: AbsPath, desired: String, sudoWrite: Boolean = false): F[Unit]
}

trait XFCE[F[_]] {
  def unsetKb(path: String): F[Unit]
}

trait Secure[F[_]] {
  def mergeVeraDir(veraVolume: AbsPath, hashFile: AbsPath, dst: AbsPath, useForHash: AbsPath => Boolean ): F[Unit]
}

trait Out[F[_]] {
  def println(str: String): F[Unit]
}