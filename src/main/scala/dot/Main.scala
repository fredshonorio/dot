package dot

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import dot.exec.Shell
import dot.impl._
import dot.misc.{aur, forHost, merge, modprobe, pac, sys, systemd}
import dot.syntax._

object Main extends IOApp {
  val Sym = Symbolic
  val Abs = AbsPath
  val vault = Abs.~ / "Tresors" / "vault2"
  val autostart = Sym.~ / ".config" / "autostart"

  def mergeVault[F[_]: Sync](name: String, dst: AbsPath, useForHash: AbsPath => Boolean = _ => true)(implicit s: Secure[F], o: Out[F]): F[Unit] =
    s.mergeVeraDir(vault / name, vault / s"$name.hash", dst, useForHash)

  def install[F[_] : Sync](implicit p: Pkg[F], sh: Shell[F], f: Files[F], out: Out[F], sec: Secure[F]): F[Unit] = List(
    // bootstrap
    f.replaceLine(Abs.ROOT / "etc" / "makepkg.conf", "PKGEXT='.pkg.tar.xz'", "PKGEXT='.pkg.tar'", sudoWrite = true), // disable compression for packages built from aur
    pac("meld", "git", "trizen"),

    // shell
    pac("zsh", "bat", "lsof", "htop", "sakura", "the_silver_searcher"),
    aur("powerline-fonts", "prezto-git", "z", "entr"),
    merge(Sym.~ / ".zshrc"),
    merge(Sym.~ / ".profile"),
    merge(Sym.~ / ".config" / "sakura" / "sakura.conf"),
    sys.shell("/bin/zsh"),

    // ssh
    mergeVault("ssh", Abs.~ / ".ssh"),
    // TODO: sshrc

    // browser
    aur("google-chrome-beta", "firefox-beta-bin", "brave-bin"),

    // backup
    pac("veracrypt", "pass"),
    aur("securefs"),
    // tresorit
    // TODO

    // git
    pac("git", "gitg", "tk", "aspell-en"),
    merge(Sym.~ / ".bin" / "unpushed"),
    merge(Sym.~ / ".gitconfig"),
    aur("git-cola"),

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
    merge(autostart / "slack.desktop"),
    mergeVault("gradle", Abs.~ / ".gradle", _.filename == "gradle.properties"),

    // ops
    pac("python2-pip", "aws-cli"),
    aur("aws-vault"),
    when(sh.slurp("pip2 show Fabric").failed) {
      sh.interactive("sudo pip2 install fabric==1.13.1").attempt
    },

    merge(Sym.~ / ".aws" / "config"),
    mergeVault("aws", Abs.~ / ".aws", _.filename == "credentials"),

    pac("docker", "docker-compose"),
    sys.addUserToGroup("docker"),
    systemd.enable("docker"),

    // desktop
    modprobe.blacklist("uvcvideo"),
    modprobe.blacklist("pcspkr"), // disable webcam and speaker
    pac("redshift", "nemo"),
    pac("xmonad", "xmonad-contrib", "xmobar", "rofi", "feh", "trayer"),
    aur(
      "ttf-iosevka",
      "noto-fonts-emoji",
      "stlarch_icons", // icons installed in /usr/share/icons/stlarch_ico
      "rofi-dmenu" // themes in /usr/share/rofi/
    ),
    merge(Sym.~ / ".xmobarrc"),
    merge(Sym.~ / ".xmonad" / "xmonad.hs"),
    merge(autostart / "Xmonad.desktop"),
    merge(Sym.~ / ".config" / "rofi/config.rasi"),
    merge(Sym.~ / ".config" / "fontconfig" / "conf.d/01-emoji.conf"),
    merge(Sym.~ / ".trayer.sh"),
    merge(autostart / "trayer.desktop"),

    pac("playerctl", "xbindkeys", "xfce4-volumed-pulse"),

    merge(Sym.~ / ".xbindkeysrc"),
    aur("spotify"),

    // ssd
    pac("util-linux"),
    systemd.enable("fstrim.timer"),

    // misc apps
    pac("vlc", "smplayer", "android-udev"),

    // quirks
    forHost("liminal") {
      aur("powertop")
    },
  ).sequence_

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val ex: ExecImpl[IO] = new ExecImpl[IO]
    implicit val sh: ShellImpl[IO] = new ShellImpl[IO](ex)
    implicit val out: OutImpl[IO] = new OutImpl[IO]
    implicit val pkg: PkgImpl[IO] = new PkgImpl[IO](ex, out)
    implicit val f: FilesImpl[IO] = new FilesImpl[IO](ex, sh)
    implicit val sec: SecureImpl[IO] = new SecureImpl[IO](sh, f, out)

    install[IO]
      .as(ExitCode.Success)
  }
}
