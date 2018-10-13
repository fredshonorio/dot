package dot

import cats.effect.Sync
import cats.implicits._
import dot.exec.Shell
import dot.syntax.{not, when}

object misc {

  def host[F[_] : Sync]()(implicit shell: Shell[F]): F[String] =
    shell.slurp("hostname").out.map(_.trim())

  def aur[F[_] : Sync](pkgs: String*)(implicit p: Pkg[F]): F[Unit] = pkgs.toList.traverse_(p.aur)

  def pac[F[_] : Sync](pkgs: String*)(implicit p: Pkg[F]): F[Unit] = pkgs.toList.traverse_(p.pac)

  def merge[F[_]](path: Symbolic)(implicit f: Files[F]): F[Unit] = f.merge(path.src, path.dst)

  object systemd {
    def enable[F[_] : Sync](service: String)(implicit sh: Shell[F]): F[Unit] =
      when(sh.slurp(s"systemctl is-enabled $service | grep enabled").failed) {
        sh.interactive(s"sudo systemctl enable $service").attempt
      }
  }

  object sys {
    def addUserToGroup[F[_] : Sync](group: String)(implicit sh: Shell[F], out: Out[F]): F[Unit] =
      when(sh.slurp(s"groups $$USER | grep $group").failed) {
        sh.interactive(s"sudo usermod -aG $group $$USER").attempt *>
          out.println(s"You have been added to the group '$group', you'll need log out so that these changes take effect")
      }

    def shell[F[_] : Sync](binary: String)(implicit sh: Shell[F]): F[Unit] = {
      val currentShell = sh.slurp("getent passwd $LOGNAME | cut -d: -f7").out.map(_.trim)
      when(currentShell.map(_ != binary)) {
        sh.interactive(s"chsh -s $binary").attempt
      }
    }
  }

  object modprobe {
    val BL = AbsPath.ROOT / "etc" / "modprobe.d" / "blacklist-fred.conf"

    def blacklist[F[_] : Sync](module: String)(implicit f: Files[F], sh: Shell[F]): F[Unit] = {
      when(not(f.exists(BL)))(sh.interactive(s"sudo install -m 644 -D /dev/null ${BL.raw}").attempt) *>
        f.ensureLine(BL, s"blacklist $module", sudoWrite = true)
    }
  }

}
