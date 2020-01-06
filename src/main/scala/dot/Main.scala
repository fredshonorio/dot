package dot

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import dot.exec.{Exec, Shell}
import dot.impl._
import dot.misc._
import dot.syntax._

object Main extends IOApp {
  val Sym = Symbolic
  val Abs = AbsPath
  val vault = Abs.~ / "Tresors" / "vault2"
  val autostart = Sym.~ / ".config" / "autostart"

  def userBinary[F[_]: Sync](name: String)(implicit f: Files[F]): F[Unit] = {
    val pth = Sym.~ / "bin" / name
    merge(pth) *> f.makeExecutable(pth.dst)
  }

  def install[F[_] : Sync](implicit p: Pkg[F], ex: Exec[F], sh: Shell[F], f: Files[F], out: Out[F]): F[Unit] = {
    val workstation = List(

      //i use playerctl and xbindkeys with different bindings on the laptop, TODO: parameterize
      pac("playerctl"),
      pac("xbindkeys") *> merge(Sym.~ / ".xbindkeysrc") *> merge(autostart / "xbindkeys.desktop"),

      pac("xfce4-volumed-pulse"), // either this or pa-applet
      merge(autostart / "slack.desktop"),
      // TODO: discord
      systemd.enable("docker")
    ).sequence_

    val quirks = host() >>= {
      case "liminal" => aur("powertop") *> pac("android-udev")
      case "witchfinder" => workstation
    }

    List(
      // bootstrap
      f.replaceLine(Abs.ROOT / "etc" / "makepkg.conf", "PKGEXT='.pkg.tar.xz'", "PKGEXT='.pkg.tar'", sudoWrite = true), // disable compression for packages built from aur
      pac("meld", "git", "yay"),
      mergeRoot(Sym.ROOT / "etc" / "pacman.d" / "hooks" / "paccache.hook"),

      // TODO asdf-vm

      // shell
      pac("zsh", "bat", "lsof", "htop", "sakura", "the_silver_searcher"),
      aur("powerline-fonts", "prezto-git", "z", "entr", "direnv"),
      merge(Sym.~ / ".zshrc"),
      merge(Sym.~ / ".profile"),
      merge(Sym.~ / ".config" / "sakura" / "sakura.conf"),
      sys.shell("/bin/zsh"),

      // ssh
      // TODO: sshrc

      // browser
      aur("firefox"),

      // backup
      pac("veracrypt", "pass"),
      aur("securefs"),
      when(not(f.exists(Abs.~ / ".local" / "share" / "tresorit"))) {
        sh.interactive("wget https://installerstorage.blob.core.windows.net/public/install/tresorit_installer.run -P ~/Downloads && sh ~/Downloads/tresorit_installer.run").attempt
      },
      merge(autostart / "tresorit.desktop"),

      // git
      pac("git", "gitg", "tk", "aspell-en"),
      userBinary("unpushed"),
      merge(Sym.~ / ".gitconfig"),

      // emacs
      pac("emacs"),
      when(not(f.exists(Abs.~ / ".emacs.d" / "core" / "prelude-core.el"))) {
        sh.interactive("curl -L https://github.com/bbatsov/prelude/raw/master/utils/installer.sh | sh").attempt
      },
      merge(Sym.~ / ".emacs.d" / "personal" / "personal.el"),
      merge(Sym.~ / ".emacs.d" / "personal" / "custom.el"),

      // dev
      pac("jdk8-openjdk", "scala", "sbt", "cloc", "gradle"),
      aur("ammonite", "dbeaver", "intellij-idea-community-edition", "slack-desktop"),

      // ops
      pac("python2-pip", "aws-cli", "terraform"),
      aur("aws-vault", "awslogs"),
      when(sh.slurp("pip2 show Fabric").failed) {
        sh.interactive("sudo pip2 install fabric==1.13.1").attempt
      },

      merge(Sym.~ / ".aws" / "config"),
      //mergeVault("aws", Abs.~ / ".aws", _.filename == "credentials"),

      pac("docker", "docker-compose"),
      sys.addUserToGroup("docker"),

      // desktop
      pac("gtk3"),
      modprobe.blacklist("uvcvideo"),
      modprobe.blacklist("pcspkr"), // disable webcam and speaker
      pac("redshift", "nemo"),
      merge(Sym.~ / ".config" / "redshift.conf"),
      merge(autostart / "redshift-gtk.desktop"),
      pac("xmonad", "xmonad-contrib", "xmobar", "feh", "trayer"),
      aur(
        "ttf-iosevka",
        "noto-fonts-emoji",
        "stlarch_icons", // icons installed in /usr/share/icons/stlarch_ico
        "rofi",
        "rofi-dmenu" // themes in /usr/share/rofi/
      ),
      merge(Sym.~ / ".xmobarrc"),
      merge(Sym.~ / ".xmonad" / "xmonad.hs"),
      merge(autostart / "Xmonad.desktop"),
      merge(Sym.~ / ".config" / "rofi" / "config.rasi"),
      merge(Sym.~ / ".config" / "fontconfig" / "conf.d" / "01-emoji.conf"),
      userBinary("run_trayer.sh"),
      merge(autostart / "trayer.desktop"),

      pac("compton"),
      merge(autostart / "compton.desktop"),

      aur("spotify"),

      // fix for: https://community.spotify.com/t5/Desktop-Linux/spotify-connect-not-working-properly-on-linux/m-p/4595982/highlight/true#M16891
      userBinary("spotify"),

      // ssd
      pac("util-linux"),
      systemd.enable("fstrim.timer"),

      // misc apps
      pac("vlc", "smplayer"),

      pac("borg", "python-llfuse"),
      /*host()
          .flatMap(hostName =>
            mergeRoot(Sym.ROOT(hostName) / "etc" / "systemd" / "system" / "borg-backup.service") *>
            mergeRoot(Sym.ROOT(hostName) / "etc" / "systemd" / "system" / "borg-backup.timer")
          ),
       */

      /*systemd.enable("borg-backup"),
      systemd.start("borg-backup.timer"),*/

      // quirks
      quirks,
    ).sequence_
  }

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val ex: ExecImpl[IO] = new ExecImpl[IO]
    implicit val sh: ShellImpl[IO] = new ShellImpl[IO](ex)
    implicit val out: OutImpl[IO] = new OutImpl[IO]
    implicit val pkg: PkgImpl[IO] = new PkgImpl[IO](ex, out)
    implicit val f: FilesImpl[IO] = new FilesImpl[IO](ex, sh)

    install[IO]
      .as(ExitCode.Success)
  }
}
