package dot

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{StandardOpenOption, Files => JFiles}
import javax.xml.bind.DatatypeConverter.parseHexBinary

import cats.effect.IO
import cats.implicits._
import dot.impl.{ExecImpl, FilesImpl, OutImpl, SecureImpl, ShellImpl}

class SecureImplTest extends org.scalatest.FunSuite {

  val exec = new ExecImpl[IO]
  val shell = new ShellImpl[IO](exec)
  val files = new FilesImpl[IO](exec, shell)
  val out = new OutImpl[IO]
  val secure = new SecureImpl[IO](shell, files, out)

  def write(f: AbsPath, st: String): Unit = {
    val bytes: Array[Byte] = st.getBytes(UTF_8)
    val _ = JFiles.write(f.path, bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)
  }

  test("hash one") {

    val tmp = JFiles.createTempDirectory("dot-test")

    {
      val file = AbsPath.unsafe(tmp.toString) / "hello"
      write(file, "hello \uD83D\uDE0A")

      val md5 = secure.hashAll(List(file)).unsafeRunSync()
      assert(md5.bytes sameElements parseHexBinary("BE222187BA18A0E436623101C538D42F"))

      val path = AbsPath.unsafe(tmp.toString) / "hash"

      val readMd5 = (secure.writeHash(path)(md5) *> secure.readHash(path)).unsafeRunSync
      assert(readMd5.bytes sameElements md5.bytes)
    }

    JFiles.list(tmp)
      .forEach(JFiles.delete(_))

    JFiles.delete(tmp)
  }

  test("hash many") {

    val tmp = JFiles.createTempDirectory("dot-test")

    {
      val hello = AbsPath.unsafe(tmp.toString) / "hello"
      write(hello, "hello \uD83D\uDE0A")

      val goodbye = AbsPath.unsafe(tmp.toString) / "goodbye"
      write(goodbye, "goodbye \uD83D\uDCA9")

      val md5 = secure.hashAll(List(hello, goodbye)).unsafeRunSync()
      assert(md5.bytes sameElements parseHexBinary("46275CF346B0801591ECA0D3BC19DCC7"))
    }

    JFiles.list(tmp)
      .forEach(JFiles.delete(_))

    JFiles.delete(tmp)
  }

}
